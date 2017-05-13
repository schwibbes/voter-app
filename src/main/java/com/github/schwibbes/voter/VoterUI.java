package com.github.schwibbes.voter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Voter;
import com.github.schwibbes.voter.util.JsonUtil;
import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Binder;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
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
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SpringUI
@Theme("mytheme")
public class VoterUI extends UI {
	private static final long serialVersionUID = 1;

	private static final Logger log = LoggerFactory.getLogger(VoterUI.class);

	private Poll poll;

	ByteArrayOutputStream jsonUpload = new ByteArrayOutputStream(10 * (2 ^ 10));

	private MenuBar menu;

	private Binder<PollViewModel> binder;

	private final List<PopupView> popups = Lists.newArrayList();

	private ListSelect<Item> rank;

	private StreamResource exportJson;

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
		final MenuItem main = menu.addItem("", VaadinIcons.MENU, null);
		main.addItem("Restart", e -> {
			log.warn("restart poll with state -> {}", poll);
			refreshData(new Poll("new"));
		});
		main.addItem("Export", e -> {
			menuExport();

		});
		main.addItem("Import", e -> {
			menuImport();
		});
	}

	private void menuImport() {
		final Window window = new Window("Import");
		window.center();
		window.setResizable(false);
		window.setModal(true);
		window.setWidth(300.0f, Unit.PIXELS);
		final VerticalLayout content = new VerticalLayout();
		final Upload upload = new Upload("Import", new Upload.Receiver() {
			private static final long serialVersionUID = 1L;

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				jsonUpload.reset();
				return jsonUpload;
			}
		});
		upload.addSucceededListener(new SucceededListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadSucceeded(SucceededEvent event) {
				refreshData(JsonUtil.load(new String(jsonUpload.toByteArray()), Poll.class));

			}
		});
		content.addComponent(upload);
		window.setContent(content);
		getUI().addWindow(window);
	}

	private void menuExport() {
		final Window window = new Window("Export");
		window.setWidth(300.0f, Unit.PIXELS);
		window.center();
		window.setModal(true);
		window.setResizable(false);
		final VerticalLayout content = new VerticalLayout();
		window.setContent(content);

		final Button downloadButton = new Button("Download");
		content.addComponent(downloadButton);

		final FileDownloader fileDownloader = new FileDownloader(prepareResource(poll));
		fileDownloader.extend(downloadButton);

		getUI().addWindow(window);
	}

	private StreamResource prepareResource(Poll poll) {
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(poll.toString().getBytes());
			}

		}, "poll.json");
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
			log.trace("{} -> {}", scoreForThisVoter, i);
			return scoreForThisVoter <= 0 || scoreForThisVoter >= 4 ? "-" : "" + scoreForThisVoter;
		}
	}
}
