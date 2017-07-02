package com.github.schwibbes.voter;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Voter;
import com.github.schwibbes.voter.util.CalculationUtil;
import com.github.schwibbes.voter.util.ConflictResolvingItemComparator;
import com.google.common.collect.Lists;
import com.google.common.base.Objects;

import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
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
		final VerticalLayout outline = new VerticalLayout();
		outline.addStyleName("bg-image");

		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		outline.addComponent(layout);
		outline.setComponentAlignment(layout, Alignment.TOP_CENTER);
		layout.setSpacing(true);
		setContent(outline);

		createHeader(layout, polls);
		final List<PollViewModel> result = polls.stream()
				.map(my -> createContent(layout, my, polls))
				.collect(toList());
		createResult(layout, polls);
		createFooter(layout);
		return result;
	}

	private void createResult(VerticalLayout layout, List<Poll> polls) {
		final Panel outline = new Panel("Result");
		layout.addComponent(outline);
		layout.setExpandRatio(outline, 6);

		final HorizontalLayout content = new HorizontalLayout();
		outline.setContent(content);
		content.setSizeFull();

		final ListSelect<ItemAndScore> rank = createRankField(content);
		final ConflictResolvingItemComparator comparator = new ConflictResolvingItemComparator(
				polls.stream().map(x -> x.getInOrder()).collect(toList()));
		final List<ItemAndScore> data = mergedResults(polls).stream().sorted(comparator).collect(toList());
		rank.setItems(data);

	}

	private List<ItemAndScore> mergedResults(List<Poll> polls) {
		return new CalculationUtil().mergePolls(
				polls,
				polls.stream().map(x -> 1).collect(toList()));
	}

	private List<Poll> loadConfiguration() {
		return config.getPolls()
				.stream()
				.map(p -> {
					Poll result = p;
					for (final Item i : config.getItems()) {
						result = result.addItem(i);
					}
					return result;
				})
				.collect(toList());
	}

	private void createHeader(VerticalLayout parent, List<Poll> polls) {
		final CssLayout header = new CssLayout();
		parent.addComponent(header);
		parent.setExpandRatio(header, 2);
		header.setSizeFull();

		createMenu(header, polls);

		final Image logo = new Image();
		logo.setSource(new ThemeResource("logo.png"));
		logo.setHeight("80px");
		logo.setStyleName("logo-margin");
		header.addComponent(logo);
	}

	private void createMenu(final Layout header, List<Poll> polls) {
		final MenuBar menu = new MenuBar();
		menu.setHeight("80px");
		header.addComponent(menu);
		final MenuItem main = menu.addItem("", null);
		main.addItem("Restart", VaadinIcons.REFRESH, e -> refreshData(renderWidgets(loadConfiguration())));
		main.addItem("Export", VaadinIcons.CLOUD_DOWNLOAD_O, e -> fileManager.menuExport(getUI(), polls));
		main.addItem("Import", VaadinIcons.CLOUD_UPLOAD_O, e -> fileManager.menuImport(getUI(), this));
	}

	private PollViewModel createContent(VerticalLayout v, Poll my, List<Poll> all) {
		final Panel outline = new Panel(my.getName());
		v.addComponent(outline);
		v.setExpandRatio(outline, 6);

		final HorizontalSplitPanel content = new HorizontalSplitPanel();
		outline.setContent(content);
		content.setSizeFull();
		content.setSplitPosition(80, Unit.PERCENTAGE);

		final PollViewModel result = new PollViewModel(my);
		result.setPopups(createVoteField(content, result.getPoll(), all));
		result.setRank(createRankField(content));
		return result;
	}

	private List<PopupView> createVoteField(final HorizontalSplitPanel content, Poll my, List<Poll> all) {
		final Layout base = new VerticalLayout();
		content.addComponent(base);

		createFirstRow(my, base);
		final List<PopupView> popups = Lists.newArrayList();
		for (final Voter voter : my.getVoters()) {
			popups.addAll(createRowForVoter(my, all, base, voter));
		}

		return popups;
	}

	private void createFirstRow(Poll my, final Layout base) {
		final HorizontalLayout row = new HorizontalLayout();
		row.addComponent(fixedSizeLabel("")); // empty corner

		for (final Item item : my.getItems()) {
			final Label label = fixedSizeLabel(item.getName());
			row.addComponent(label);
		}
		base.addComponent(row);
	}

	private Label fixedSizeLabel(final String caption) {
		final Label label = new Label(caption);
		label.setWidth("100px");
		return label;
	}

	private Collection<PopupView> createRowForVoter(Poll my,
			List<Poll> all,
			final Layout base,
			final Voter voter) {
		final HorizontalLayout row = new HorizontalLayout();
		base.addComponent(row);

		row.addComponent(fixedSizeLabel(voter.getName()));

		final List<PopupView> popups = Lists.newArrayList();
		for (final Item item : my.getItems()) {
			if (Objects.equal(item.getName(), voter.getName())){
				row.addComponent(fixedSizeLabel("-"));
			} else {
				final PopupView popup = new PopupView(new VoteComponent(my, all, voter, item, this));
				row.addComponent(popup);
				popups.add(popup);
			}
		}
		return popups;
	}

	private ListSelect<ItemAndScore> createRankField(final AbstractComponentContainer content) {
		final VerticalLayout rankField = new VerticalLayout();
		content.addComponent(rankField);
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
