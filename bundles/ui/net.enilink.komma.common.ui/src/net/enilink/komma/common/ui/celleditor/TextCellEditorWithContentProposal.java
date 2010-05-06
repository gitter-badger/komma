package net.enilink.komma.common.ui.celleditor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.enilink.commons.ui.CommonsUi;

public class TextCellEditorWithContentProposal extends TextCellEditor {
	private ContentProposalAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open

	public TextCellEditorWithContentProposal(Composite parent,
			IContentProposalProvider contentProposalProvider,
			char[] autoActivationCharacters) {
		super(parent);

		enableContentProposal(contentProposalProvider, autoActivationCharacters);
	}

	private void enableContentProposal(
			IContentProposalProvider contentProposalProvider,
			char[] autoActivationCharacters) {
		List<Object> args = new ArrayList<Object>(Arrays.asList(text,
				new TextContentAdapter(), contentProposalProvider));
		Constructor<ContentProposalAdapter> constructor;
		try {
			if (CommonsUi.IS_RAP_RUNNING) {
				constructor = ContentProposalAdapter.class.getConstructor(
						Control.class, IControlContentAdapter.class,
						IContentProposalProvider.class, char[].class);
			} else {
				Class<?> keyStrokeClass = getClass().getClassLoader()
						.loadClass("org.eclipse.jface.bindings.keys.KeyStroke");
				constructor = ContentProposalAdapter.class.getConstructor(
						Control.class, IControlContentAdapter.class,
						IContentProposalProvider.class, keyStrokeClass,
						char[].class);

				Method getInstance = keyStrokeClass.getMethod("getInstance",
						int.class, int.class);

				args.add(getInstance.invoke(null, SWT.CTRL, ' '));
			}

			args.add(autoActivationCharacters);
			contentProposalAdapter = constructor.newInstance(args.toArray());
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to initialize content proposals", e);
		}

		// Listen for popup open/close events to be able to handle focus events
		// correctly
		contentProposalAdapter
				.addContentProposalListener(new IContentProposalListener2() {
					public void proposalPopupClosed(
							ContentProposalAdapter adapter) {
						popupOpen = false;
					}

					public void proposalPopupOpened(
							ContentProposalAdapter adapter) {
						popupOpen = true;
					}
				});
	}

	/**
	 * Return the {@link ContentProposalAdapter} of this cell editor.
	 * 
	 * @return the {@link ContentProposalAdapter}
	 */
	public ContentProposalAdapter getContentProposalAdapter() {
		return contentProposalAdapter;
	}

	@Override
	protected void focusLost() {
		if (!popupOpen) {
			// Focus lost deactivates the cell editor.
			// This must not happen if focus lost was caused by activating
			// the completion proposal popup.
			super.focusLost();
		}
	}

	@Override
	protected boolean dependsOnExternalFocusListener() {
		// Always return false;
		// Otherwise, the ColumnViewerEditor will install an additional focus
		// listener
		// that cancels cell editing on focus lost, even if focus gets lost due
		// to
		// activation of the completion proposal popup. See also bug 58777.
		return false;
	}
}
