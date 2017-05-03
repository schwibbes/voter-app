package com.github.schwibbes.voter;

import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Binder;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializableComparator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("mytheme")
public class VoterUI extends UI {
	private static final long serialVersionUID = 1;

	private static final Logger log = LoggerFactory.getLogger(VoterUI.class);

	private Poll poll;

	private MenuBar menu;

	private Binder<PollViewModel> binder;

	private final Command restart = e -> {
		log.warn("restart poll with state -> {}", poll);
		refreshData(new Poll("new"));
	};

	private ListDataProvider<Item> rankData;

	private final List<PopupView> popups = Lists.newArrayList();

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		poll = new Poll("new Poll");

		binder = new Binder<>(PollViewModel.class);
		binder.addValueChangeListener(new ValueChangeListener<Poll>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<Poll> event) {
				System.out.println(event);
			}
		});

		poll = loadConfiguration(poll);

		final VerticalLayout v = new VerticalLayout();
		v.setWidth("100%");
		v.setHeight("100%");
		setContent(v);

		createHeader(v);
		createContent(v);
		createFooter(v);

		refreshData(poll);
	}

	private Poll loadConfiguration(Poll p) {
		final Poll[] result = new Poll[] { p };
		IntStream.range(1, 6).forEach(i -> result[0] = result[0].addItem(new Item("team-" + i)));
		IntStream.range(1, 4).forEach(i -> result[0] = result[0].addVoter(new Voter("voter-" + i)));
		return result[0];
	}

	private void createHeader(VerticalLayout body) {
		final HorizontalLayout header = new HorizontalLayout();
		body.addComponent(header);
		body.setExpandRatio(header, 2);
		header.setSizeFull();
		header.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		header.addComponent(new Image("Logo"));
		final Label pageTitle = new Label("Voter<app>");
		header.addComponent(pageTitle);
		header.setComponentAlignment(pageTitle, Alignment.MIDDLE_LEFT);
		createMenu(header);
	}

	private void createMenu(final HorizontalLayout header) {
		menu = new MenuBar();
		header.addComponent(menu);
		final MenuItem main = menu.addItem("-", null);
		main.addItem("Restart", restart);
		main.addItem("Done", null);
	}

	private void createContent(VerticalLayout v) {
		final HorizontalLayout content = new HorizontalLayout();
		v.addComponent(content);
		v.setExpandRatio(content, 6);
		content.setSizeFull();

		createRankField(content);
		createVoteField(content);
	}

	private void createVoteField(final HorizontalLayout content) {
		final VerticalLayout voteField = new VerticalLayout();
		content.addComponent(voteField);
		content.setExpandRatio(voteField, 1.0f);

		final GridLayout grid = grid(voteField);

		for (final Item item : poll.getItems()) {
			grid.addComponent(new Label(item.getName()));
		}

		for (final Voter voter : poll.getVoters()) {

			grid.addComponent(new Label(voter.getName()));

			for (final Item item : poll.getItems()) {
				final PopupView popup = new PopupView(new PopupTextFieldContent(voter, item));
				grid.addComponent(popup);
				popups.add(popup);
			}
		}
	}

	private GridLayout grid(final VerticalLayout voteField) {
		final GridLayout grid = new GridLayout(1 + poll.getItems().size(), 1 + poll.getVoters().size());
		voteField.addComponent(grid);
		voteField.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

		grid.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		grid.addComponent(new Label()); // empty corner
		return grid;
	}

	private void createRankField(final HorizontalLayout content) {
		final VerticalLayout rankField = new VerticalLayout();
		content.addComponent(rankField);
		content.setExpandRatio(rankField, 0.2f);
		rankData = DataProvider.ofCollection(poll.getItems());
		final ListSelect<Item> rank = new ListSelect<>("", rankData);
		rank.setItemCaptionGenerator(Item::getName);
		rank.setWidth("100%");
		rankField.addComponent(rank);
	}

	private void createFooter(VerticalLayout v) {
		final HorizontalLayout footer = new HorizontalLayout();
		v.addComponent(footer);
		v.setExpandRatio(footer, 1);
		footer.setSizeFull();
	}

	public void refreshData(Poll p) {
		try {
			poll = p;
			final PollViewModel vm = new PollViewModel();
			vm.setRank(Sets.newLinkedHashSet(p.getInOrder()));
			vm.setName(p.getName() + "-" + p.get1st());
			log.warn("" + vm);
			binder.readBean(vm);
			refreshList();
			popups.forEach(PopupView::markAsDirty);
		} catch (final Exception e) {
			log.error("problem during writeBean", e);
		}
	}

	private void refreshList() {
		rankData.setSortComparator(new SerializableComparator<Item>() {

			private static final long serialVersionUID = 1L;

			@Override
			public int compare(Item o1, Item o2) {
				return poll.getInOrder().indexOf(o2) - poll.getInOrder().indexOf(o1);
			}

		});
		rankData.refreshAll();
	}

	private class PopupTextFieldContent implements PopupView.Content {
		private static final long serialVersionUID = 1L;
		private final HorizontalLayout layout;
		private final Voter v;
		private final Item i;

		private PopupTextFieldContent(Voter v, Item i) {
			this.v = v;
			this.i = i;
			layout = new HorizontalLayout();
			layout.addComponent(new Button("1st", e -> {
				refreshData(poll.vote(v, i, 1));
			}));
			layout.addComponent(new Button("2nd", e -> {
				refreshData(poll.vote(v, i, 2));
			}));
			layout.addComponent(new Button("3rd", e -> {
				refreshData(poll.vote(v, i, 3));
			}));
		}

		@Override
		public final Component getPopupComponent() {
			return layout;
		}

		@Override
		public final String getMinimizedValueAsHTML() {
			final int scoreForThisVoter = 1 + poll.queryVote(v, i);
			log.info("{} -> {}", scoreForThisVoter, i);
			return scoreForThisVoter <= 0 || scoreForThisVoter >= 4 ? "-" : "" + scoreForThisVoter;
		}
	}
}
