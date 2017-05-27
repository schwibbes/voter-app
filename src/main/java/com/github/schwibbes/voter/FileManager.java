package com.github.schwibbes.voter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.data.Poll;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FileManager {

	private static final Logger log = LoggerFactory.getLogger(FileManager.class);

	public void menuImport(UI ui, UploadHandler listener) {
		final Window window = new Window("Import");
		window.center();
		window.setResizable(false);
		window.setModal(true);
		window.setWidth(300.0f, Unit.PIXELS);
		final VerticalLayout content = new VerticalLayout();
		final ByteArrayOutputStream jsonUpload = new ByteArrayOutputStream(10 * (2 ^ 10));
		final Upload upload = new Upload("Import", new Upload.Receiver() {
			private static final long serialVersionUID = 1L;

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				jsonUpload.reset();
				return jsonUpload;
			}
		});
		upload.addSucceededListener(event -> {
			try {
				listener.accept(jsonUpload.toString());
			} finally {
				if (jsonUpload != null) {
					try {
						jsonUpload.close();
					} catch (final IOException e) {
						log.info("cannot close stream");
					}
				}
			}
		});
		content.addComponent(upload);
		window.setContent(content);
		ui.addWindow(window);
	}

	public void menuExport(UI ui, Poll poll) {
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

		ui.addWindow(window);
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

	public static interface UploadHandler {
		void accept(String json);
	}
}
