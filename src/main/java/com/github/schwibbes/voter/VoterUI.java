package com.github.schwibbes.voter;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
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
public class VoterUI extends UI implements UpdateHandler, InitializingBean {
	private static final long serialVersionUID = 1;

	private static final Logger log = LoggerFactory.getLogger(VoterUI.class);

	private FileManager fileManager;

	@Autowired
	private Config config;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		fileManager = new FileManager();
		refreshData(renderWidgets(loadConfiguration()));
	}

	private List<PollViewModel> renderWidgets(List<Poll> polls) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight("100%");
		setContent(layout);

		createHeader(layout, polls);
		final List<PollViewModel> result = polls.stream()
				.map(my -> createContent(layout, my, polls))
				.collect(toList());
		createFooter(layout);
		return result;
	}

	private List<Poll> loadConfiguration() {
		return config.getPolls()
				.stream()
				// .map(p -> new PollViewModel(p))
				.collect(toList());
	}

	private void createHeader(VerticalLayout body, List<Poll> polls) {
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
		createMenu(header, polls);
	}

	private void createMenu(final HorizontalLayout header, List<Poll> polls) {
		final MenuBar menu = new MenuBar();
		header.addComponent(menu);
		final MenuItem main = menu.addItem("", VaadinIcons.MENU, null);
		main.addItem("Restart", e -> refreshData(renderWidgets(loadConfiguration())));
		main.addItem("Export", e -> fileManager.menuExport(getUI(), polls));
		main.addItem("Import", e -> fileManager.menuImport(getUI(), this));
	}

	private PollViewModel createContent(VerticalLayout v, Poll my, List<Poll> all) {
		final HorizontalLayout content = new HorizontalLayout();
		v.addComponent(content);
		v.setExpandRatio(content, 6);
		content.setSizeFull();

		final PollViewModel result = new PollViewModel(my);
		result.setRank(createRankField(content, my));
		result.setPopups(createVoteField(content, result.getPoll(), all));
		return result;
	}

	private List<PopupView> createVoteField(final HorizontalLayout content, Poll my, List<Poll> all) {
		final VerticalLayout voteField = new VerticalLayout();
		content.addComponent(voteField);
		content.setExpandRatio(voteField, 1.0f);

		final GridLayout grid = grid(voteField, my);

		for (final Item item : my.getItems()) {
			grid.addComponent(new Label(item.getName()));
		}

		final List<PopupView> popups = Lists.newArrayList();
		for (final Voter voter : my.getVoters()) {

			grid.addComponent(new Label(voter.getName()));

			for (final Item item : my.getItems()) {
				final PopupView popup = new PopupView(new VoteComponent(my, all, voter, item, this));
				grid.addComponent(popup);
				popups.add(popup);
			}
		}

		return popups;
	}

	private GridLayout grid(final VerticalLayout voteField, Poll p) {
		final GridLayout grid = new GridLayout(1 + p.getItems().size(), 1 + p.getVoters().size());
		voteField.addComponent(grid);
		voteField.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

		grid.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		grid.addComponent(new Label()); // empty corner
		return grid;
	}

	private ListSelect<ItemAndScore> createRankField(final HorizontalLayout content, Poll p) {
		final VerticalLayout rankField = new VerticalLayout();
		content.addComponent(rankField);
		content.setExpandRatio(rankField, 0.2f);
		final ListSelect<ItemAndScore> rank = new ListSelect<>();
		rank.setItemCaptionGenerator(x -> String.format("%s (%d)", x.getItem().getName(), x.getScore()));
		rank.setWidth("100%");
		rankField.addComponent(rank);
		return rank;
	}

	private void createFooter(VerticalLayout v) {
		final HorizontalLayout footer = new HorizontalLayout();
		v.addComponent(footer);
		v.setExpandRatio(footer, 1);
		footer.setSizeFull();
	}

	public void refreshData(List<PollViewModel> polls2) {
		try {
			polls2.forEach(p -> p.updateDisplay());
		} catch (final Exception e) {
			log.error("problem during refresh", e);
		}
	}

	@Override
	public void updatePolls(List<Poll> updated) {
		refreshData(renderWidgets(updated));
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println(config.getPolls());
	}
}

interface UpdateHandler {
	void updatePolls(List<Poll> updated);
}
