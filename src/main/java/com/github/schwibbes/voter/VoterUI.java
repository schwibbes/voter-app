package com.github.schwibbes.voter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.FileManager.UploadHandler;
import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
import com.github.schwibbes.voter.util.JsonUtil;
import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
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
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("mytheme")
public class VoterUI extends UI implements UploadHandler {
	private static final long serialVersionUID = 1;

	private static final Logger log = LoggerFactory.getLogger(VoterUI.class);

	private Poll poll;
	private final List<PopupView> popups = Lists.newArrayList();
	private ListSelect<ItemAndScore> rank;
	private FileManager fileManager;

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		poll = loadConfiguration();
		createLayout();
		refreshData(poll);
		fileManager = new FileManager();
	}

	private void createLayout() {
		final VerticalLayout v = new VerticalLayout();
		v.setWidth("100%");
		v.setHeight("100%");
		setContent(v);

		createHeader(v);
		createContent(v);
		createFooter(v);
	}

	private Poll loadConfiguration() {
		// TODO load from config
		final AtomicReference<Poll> result = new AtomicReference<>(new Poll("my-vote"));
		IntStream.range(1, 6).forEach(i -> result.set(result.get().addItem(new Item("team-" + i))));
		IntStream.range(1, 4).forEach(i -> result.set(result.get().addVoter(new Voter("voter-" + i))));
		return result.get();
	}

	private void createHeader(VerticalLayout body) {
		final HorizontalLayout header = new HorizontalLayout();
		body.addComponent(header);
		body.setExpandRatio(header, 2);
		header.setSizeFull();
		header.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		header.addComponent(new Image("Logo"));
		// TODO: load logo
		final Label pageTitle = new Label("Voter<app>");
		header.addComponent(pageTitle);
		header.setComponentAlignment(pageTitle, Alignment.MIDDLE_LEFT);
		createMenu(header);
	}

	private void createMenu(final HorizontalLayout header) {

		final MenuBar menu = new MenuBar();
		header.addComponent(menu);
		final MenuItem main = menu.addItem("", VaadinIcons.MENU, null);
		main.addItem("Restart", e -> refreshData(new Poll("new")));

		main.addItem("Export", e -> fileManager.menuExport(getUI(), poll));
		main.addItem("Import", e -> fileManager.menuImport(getUI(), this));
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
		rank = new ListSelect<>();
		rank.setItemCaptionGenerator(x -> String.format("%s (%d)", x.getItem().getName(), x.getScore()));
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
			// final PollViewModel vm = new PollViewModel();
			// vm.setRank(Sets.newLinkedHashSet(p.getInOrder()));
			// vm.setName(p.getName() + "-" + p.get1st());
			// binder.readBean(vm);
			rank.clear();
			rank.setItems(poll.getInOrder());
			popups.forEach(PopupView::markAsDirty);
		} catch (final Exception e) {
			log.error("problem during writeBean", e);
		}
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

			poll.getScores().forEach(score -> {
				layout.addComponent(new Button(score + "Punkte", e -> {
					refreshData(poll.addVote(v, i, score));
				}));
			});

		}

		@Override
		public final Component getPopupComponent() {
			return layout;
		}

		@Override
		public final String getMinimizedValueAsHTML() {
			final Optional<Vote> vote = poll.getVoteByVoterAndItem(v, i);
			return vote.isPresent() ? ("" + vote.get().getItemAndScore().getScore()) : "-";
		}
	}

	@Override
	public void accept(String json) {
		refreshData(JsonUtil.load(json, Poll.class));
	}
}
