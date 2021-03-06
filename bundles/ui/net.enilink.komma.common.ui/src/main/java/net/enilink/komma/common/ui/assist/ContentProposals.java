package net.enilink.komma.common.ui.assist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

public class ContentProposals {
	public static IContentProposalProvider NULL_PROPOSAL_PROVIDER = new IContentProposalProvider() {
		public IContentProposal[] getProposals(String contents, int position) {
			return new IContentProposal[0];
		}
	};

	public static ContentProposalAdapter enableContentProposal(
			final Control control,
			IContentProposalProvider contentProposalProvider,
			char[] autoActivationCharacters) {
		KeyStroke triggerKeyStroke = KeyStroke.getInstance(SWT.CTRL, ' ');

		final IControlContentAdapter controlContentAdapter = new TextContentAdapter();
		final ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(
				control, controlContentAdapter, contentProposalProvider,
				triggerKeyStroke, autoActivationCharacters);
		proposalAdapter
				.addContentProposalListener(new IContentProposalListener() {
					public void proposalAccepted(IContentProposal proposal) {
						if (proposal instanceof IContentProposalExt
								&& ((IContentProposalExt) proposal).getType() == IContentProposalExt.Type.REPLACE) {
							controlContentAdapter.setControlContents(control,
									proposal.getContent(),
									proposal.getCursorPosition());
							return;
						}
						if (proposalAdapter.getProposalAcceptanceStyle() == ContentProposalAdapter.PROPOSAL_IGNORE) {
							// default is insert
							controlContentAdapter.insertControlContents(
									control, proposal.getContent(),
									proposal.getCursorPosition());
						}
					}
				});
		return proposalAdapter;
	}
}
