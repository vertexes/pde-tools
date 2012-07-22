package net.jeeeyul.pdetools.clipboard.internal;

import java.util.Date;

import net.jeeeyul.pdetools.clipboard.IClipboardService;
import net.jeeeyul.pdetools.clipboard.model.clipboard.ClipHistory;
import net.jeeeyul.pdetools.clipboard.model.clipboard.ClipboardEntry;
import net.jeeeyul.pdetools.clipboard.model.clipboard.ClipboardFactory;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

public class ClipboardServiceImpl implements IClipboardService {
	private static IClipboardService INSTANCE;
	private static ILock lock = Job.getJobManager().newLock();

	public static void initailze() {
		lock.acquire();
		try {
			if (INSTANCE == null) {
				INSTANCE = new ClipboardServiceImpl();
			}
		} finally {
			lock.release();
		}
	}

	public static IClipboardService getInstance() {
		lock.acquire();
		try {
			if (INSTANCE == null) {
				initailze();
			}
		} finally {
			lock.release();
		}
		return INSTANCE;
	}

	private ClipHistory history;
	private CopyActionDetector detector;
	private Clipboard nativeClipboard;

	@Override
	public Clipboard getNativeClipboard() {
		if (nativeClipboard == null) {
			nativeClipboard = new Clipboard(Display.getDefault());
		}
		return nativeClipboard;
	}

	@Override
	public ClipHistory getHistory() {
		if (history == null) {
			try {

			}

			catch (Exception e) {
				history = ClipboardFactory.eINSTANCE.createClipHistory();
			}
		}
		return history;
	}

	public ClipboardServiceImpl() {
		detector = new CopyActionDetector();
		detector.setCopyHandler(new Procedure1<ExecutionEvent>() {
			@Override
			public void apply(ExecutionEvent event) {
				handleCopy(event);
			}
		});
	}

	protected void handleCopy(ExecutionEvent event) {
		boolean hasTextContents = hasDataFor(getTextTransfer());
		if (!hasTextContents) {
			return;
		}

		String textContents = (String) getNativeClipboard().getContents(getTextTransfer());

		if (!getHistory().getEntries().isEmpty()) {
			if (getHistory().getEntries().get(0).getTextContent().equals(textContents)) {
				return;
			}
		}
		ClipboardEntry entry = createClipEntry();
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part != null) {
			entry.setImageData(part.getTitleImage().getImageData());
			entry.setPartId(part.getSite().getId());
		}
		entry.setTakenTime(new Date());
		getHistory().getEntries().add(0, entry);
	}

	protected TextTransfer getTextTransfer() {
		return TextTransfer.getInstance();
	}

	protected RTFTransfer getRTFTransfer() {
		return RTFTransfer.getInstance();
	}

	public void dispose() {
		nativeClipboard.dispose();
		detector.dispose();
	}

	public ClipboardEntry createClipEntry() {

		ClipboardEntry entry = ClipboardFactory.eINSTANCE.createClipboardEntry();
		entry.setTextContent((String) getNativeClipboard().getContents(getTextTransfer()));

		if (hasDataFor(getRTFTransfer())) {
			entry.setRtfContent((String) getNativeClipboard().getContents(getRTFTransfer()));
		}

		return entry;
	}

	private boolean hasDataFor(Transfer transfer) {
		TransferData[] availableTypes = getNativeClipboard().getAvailableTypes();
		for (TransferData eachData : availableTypes) {
			if (transfer.isSupportedType(eachData)) {
				return true;
			}
		}
		return false;
	}
}
