/*******************************************************************************
 * Copyright (c) 2014 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.komma.edit.ui.rcp.commands;

import net.enilink.komma.edit.ui.commands.AbstractHandler;
import net.enilink.komma.edit.ui.wizards.RefactorMoveWizard;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Command handler to refactor/move resources into a different model.
 */
public class RefactorMoveHandler extends AbstractHandler {
	@Override
	public void execute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		RefactorMoveWizard wizard = new RefactorMoveWizard(
				getEditingDomainChecked(event), workbench,
				(IStructuredSelection) workbench.getActiveWorkbenchWindow()
						.getSelectionService().getSelection());

		// create and open the wizard dialog
		WizardDialog dialog = new WizardDialog(workbench
				.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}
}