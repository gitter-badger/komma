/*******************************************************************************
 * Copyright (c) 2009 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/

package net.enilink.komma.owl.editor.ontology;

import java.util.Collection;
import java.util.Iterator;

import net.enilink.komma.common.command.CommandResult;
import net.enilink.komma.common.command.SimpleCommand;
import net.enilink.komma.common.notify.INotification;
import net.enilink.komma.common.notify.INotificationListener;
import net.enilink.komma.common.notify.NotificationFilter;
import net.enilink.komma.core.INamespace;
import net.enilink.komma.core.KommaException;
import net.enilink.komma.core.Namespace;
import net.enilink.komma.core.URIImpl;
import net.enilink.komma.edit.ui.properties.IEditUIPropertiesImages;
import net.enilink.komma.edit.ui.properties.KommaEditUIPropertiesPlugin;
import net.enilink.komma.edit.ui.provider.ExtendedImageRegistry;
import net.enilink.komma.edit.ui.views.AbstractEditingDomainPart;
import net.enilink.komma.model.IModel;
import net.enilink.komma.model.event.INamespaceNotification;
import net.enilink.komma.owl.editor.OWLEditorPlugin;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;

public class NamespacesPart extends AbstractEditingDomainPart {
	private IModel model;
	private TableViewer namespaceViewer;
	private Action deleteItemAction, addItemAction;
	private NamespaceListener listener = new NamespaceListener();

	enum ColumnType {
		Prefix, Namespace
	}

	abstract class NamespaceCommand extends SimpleCommand {
		@Override
		protected CommandResult doExecuteWithResult(
				IProgressMonitor progressMonitor, IAdaptable info)
				throws ExecutionException {
			try {
				modifyNamespace();
				// force model to be modified
				model.setModified(true);
			} catch (KommaException e) {
				return CommandResult.newErrorCommandResult(e);
			}
			return CommandResult.newOKCommandResult();
		}

		protected abstract void modifyNamespace();
	}

	private class NamespaceEditingSupport extends EditingSupport {
		ColumnType columnType;
		TextCellEditor cellEditor;

		public NamespaceEditingSupport(TableViewer viewer, ColumnType columnType) {
			super(viewer);
			this.columnType = columnType;
			this.cellEditor = new TextCellEditor(viewer.getTable(), SWT.NONE);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			final INamespace namespace = (INamespace) element;
			switch (columnType) {
			case Prefix:
				return namespace.getPrefix();
			case Namespace:
				return namespace.getURI().toString();
			}
			return "";
		}

		@Override
		protected void setValue(Object element, final Object value) {
			final INamespace namespace = (INamespace) element;
			switch (columnType) {
			case Prefix:
				for (INamespace existing : model.getManager().getNamespaces()) {
					if (value.equals(existing.getPrefix())) {
						return;
					}
				}
				execute(new NamespaceCommand() {
					@Override
					protected void modifyNamespace() {
						model.getManager().removeNamespace(
								namespace.getPrefix());
						model.getManager().setNamespace((String) value,
								namespace.getURI());
					}
				});
				break;
			case Namespace:
				execute(new NamespaceCommand() {
					@Override
					protected void modifyNamespace() {
						model.getManager().removeNamespace(
								namespace.getPrefix());
						model.getManager().setNamespace(namespace.getPrefix(),
								URIImpl.createURI((String) value));
					}
				});
			}

		}

	}

	private boolean execute(NamespaceCommand command) {
		try {
			getEditingDomain().getCommandStack().execute(command, null, null);
			return true;
		} catch (ExecutionException e) {
			OWLEditorPlugin.INSTANCE.log(e);
		}
		return false;
	}

	public void removeNamespace(final INamespace element) {
		execute(new NamespaceCommand() {
			@Override
			protected void modifyNamespace() {
				model.getManager().removeNamespace(element.getPrefix());
			}
		});
	}

	private class NamespaceListener implements
			INotificationListener<INotification> {
		@Override
		public NotificationFilter<INotification> getFilter() {
			return NotificationFilter.instanceOf(INamespaceNotification.class);
		}

		@Override
		public void notifyChanged(
				Collection<? extends INotification> notifications) {
			namespaceViewer.refresh();
		}
	};

	private class NamespaceLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return getImage(element);
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof INamespace) {
				switch (columnIndex) {
				case 0:
					return ((INamespace) element).getPrefix();
				case 1:
					return ((INamespace) element).getURI().toString();
				}
			}
			return "";
		}
	}

	public void createContents(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		createActions(parent);

		Table table = getWidgetFactory().createTable(parent,
				SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		namespaceViewer = new TableViewer(table);
		// setup table columns [prefix, namespace]
		TableViewerColumn column = new TableViewerColumn(namespaceViewer,
				SWT.LEFT);
		column.getColumn().setText("Prefix");
		column.setEditingSupport(new NamespaceEditingSupport(namespaceViewer,
				ColumnType.Prefix));

		column = new TableViewerColumn(namespaceViewer, SWT.LEFT);
		column.getColumn().setAlignment(SWT.LEFT);
		column.getColumn().setText("Namespace");
		column.setEditingSupport(new NamespaceEditingSupport(namespaceViewer,
				ColumnType.Namespace));

		namespaceViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IModel) {
					return ((IModel) inputElement).getManager().getNamespaces()
							.toList().toArray();
				}
				return new Object[0];
			}
		});
		namespaceViewer.setLabelProvider(new NamespaceLabelProvider());
		namespaceViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (deleteItemAction != null)
							deleteItemAction.setEnabled(!event.getSelection()
									.isEmpty());
					}
				});
		namespaceViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				INamespace ns1 = (INamespace) e1;
				INamespace ns2 = (INamespace) e2;
				return ns1.getPrefix().compareToIgnoreCase(ns2.getPrefix());
			}
		});
		namespaceViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public void createActions(Composite parent) {
		IToolBarManager toolBarManager = (IToolBarManager) getForm()
				.getAdapter(IToolBarManager.class);
		ToolBarManager ownManager = null;
		if (toolBarManager == null) {
			toolBarManager = ownManager = new ToolBarManager(SWT.HORIZONTAL);
			ToolBar toolBar = ownManager.createControl(parent);
			getWidgetFactory().adapt(toolBar);
			toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, true,
					false));
		}

		addItemAction = new Action("Add") {
			public void run() {
				addItem();
			}
		};
		addItemAction.setImageDescriptor(ExtendedImageRegistry.getInstance()
				.getImageDescriptor(
						KommaEditUIPropertiesPlugin.INSTANCE
								.getImage(IEditUIPropertiesImages.ADD)));
		toolBarManager.add(addItemAction);

		deleteItemAction = new Action("Remove") {
			public void run() {
				deleteItem();
			}
		};
		deleteItemAction.setImageDescriptor(ExtendedImageRegistry.getInstance()
				.getImageDescriptor(
						KommaEditUIPropertiesPlugin.INSTANCE
								.getImage(IEditUIPropertiesImages.REMOVE)));
		deleteItemAction.setEnabled(false);
		toolBarManager.add(deleteItemAction);

		if (ownManager != null) {
			ownManager.update(true);
		}
	}

	void addItem() {
		INamespace newNs = new Namespace("", "http://");
		namespaceViewer.add(newNs);
		namespaceViewer.setSelection(new StructuredSelection(newNs), true);
	}

	void deleteItem() {
		IStructuredSelection selection = (IStructuredSelection) namespaceViewer
				.getSelection();
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			INamespace namespace = (INamespace) it.next();
			removeNamespace(namespace);
		}
	}

	@Override
	public boolean setFocus() {
		if (namespaceViewer != null && namespaceViewer.getTable().setFocus()) {
			return true;
		}
		return super.setFocus();
	}

	public void setInput(Object input) {
		if (this.model != null) {
			this.model.getModelSet().removeListener(listener);
		}
		this.model = (IModel) input;
		if (this.model != null) {
			this.model.getModelSet().addListener(listener);
		}
		setStale(true);
	}

	@Override
	public void refresh() {
		namespaceViewer.setInput(model);
		for (TableColumn column : namespaceViewer.getTable().getColumns()) {
			column.pack();
		}
		super.refresh();
	}

	@Override
	public void dispose() {
		if (this.model != null) {
			this.model.getModelSet().removeListener(listener);
			this.model = null;
		}
		super.dispose();
	}
}
