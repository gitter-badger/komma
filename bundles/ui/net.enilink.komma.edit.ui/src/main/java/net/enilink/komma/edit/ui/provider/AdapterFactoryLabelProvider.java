/**
 * <copyright> 
 *
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors: 
 *   IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: AdapterFactoryLabelProvider.java,v 1.7 2008/12/23 15:12:58 emerks Exp $
 */
package net.enilink.komma.edit.ui.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import net.enilink.komma.common.adapter.IAdapterFactory;
import net.enilink.komma.common.notify.INotification;
import net.enilink.komma.common.notify.INotificationListener;
import net.enilink.komma.common.notify.INotifier;
import net.enilink.komma.common.notify.NotificationFilter;
import net.enilink.komma.edit.KommaEditPlugin;
import net.enilink.komma.edit.provider.IItemColorProvider;
import net.enilink.komma.edit.provider.IItemFontProvider;
import net.enilink.komma.edit.provider.IItemLabelProvider;
import net.enilink.komma.edit.provider.ITableItemColorProvider;
import net.enilink.komma.edit.provider.ITableItemFontProvider;
import net.enilink.komma.edit.provider.ITableItemLabelProvider;
import net.enilink.komma.edit.provider.IViewerNotification;

/**
 * This label provider wraps an AdapterFactory and it delegates its JFace
 * provider interfaces to corresponding adapter-implemented item provider
 * interfaces. All method calls to the various label provider interfaces are
 * delegated to interfaces implemented by the adapters generated by the
 * AdapterFactory. {@link ILabelProvider} is delegated to
 * {@link IItemLabelProvider}; {@link IFontProvider} is delegated to
 * {@link IItemFontProvider}; {@link IColorProvider} is delegated to
 * {@link IItemColorProvider}; {@link ITableLabelProvider} is delegated to
 * {@link ITableItemLabelProvider}; and {@link ITableFontProvider} is delegated
 * to {@link ITableItemFontProvider}. and {@link ITableColorProvider} is
 * delegated to {@link ITableItemColorProvider}.
 * <p>
 * The label provider has no mechanism for notifying the viewer of changes. As
 * long as the AdapterFactory is also used in an AdapterFactoryContentProvider,
 * this won't be a problem, because notifications will be forward as a result of
 * that.
 */
public class AdapterFactoryLabelProvider implements ILabelProvider,
		ITableLabelProvider, INotificationListener<INotification> {
	/**
	 * An extended version of the adapter factory label provider that also
	 * provides for fonts.
	 */
	public static class FontProvider extends AdapterFactoryLabelProvider
			implements IFontProvider, ITableFontProvider {
		/**
		 * Construct an instance that wraps the given factory and specifies the
		 * given default font.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param defaultFont
		 *            the font that will be used when no font is specified.
		 */
		public FontProvider(IAdapterFactory adapterFactory, Font defaultFont) {
			super(adapterFactory);
			setDefaultFont(defaultFont);
		}

		/**
		 * Construct an instance that wraps the given factory and uses the font
		 * of the viewer's control.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param viewer
		 *            the viewer for which the control's font should be used.
		 */
		public FontProvider(IAdapterFactory adapterFactory, Viewer viewer) {
			this(adapterFactory, viewer.getControl().getFont());
		}
	}

	/**
	 * An extended version of the adapter factory label provider that also
	 * provides for colors.
	 */
	public static class ColorProvider extends AdapterFactoryLabelProvider
			implements IColorProvider, ITableColorProvider {
		/**
		 * Construct an instance that wraps the given factory and specifies the
		 * given default colors.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param defaultForeground
		 *            the foreground color that will be used when no foreground
		 *            color is specified.
		 * @param defaultBackground
		 *            the background color that will be used when no background
		 *            color is specified.
		 */
		public ColorProvider(IAdapterFactory adapterFactory,
				Color defaultForeground, Color defaultBackground) {
			super(adapterFactory);
			setDefaultForeground(defaultForeground);
			setDefaultBackground(defaultBackground);
		}

		/**
		 * Construct an instance that wraps the given factory and uses the
		 * colors of the viewer's control.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param viewer
		 *            the viewer for which the control's color should be used.
		 */
		public ColorProvider(IAdapterFactory adapterFactory, Viewer viewer) {
			this(adapterFactory, viewer.getControl().getForeground(), viewer
					.getControl().getBackground());
		}
	}

	/**
	 * An extended version of the adapter factory label provider that also
	 * provides for fonts and colors.
	 */
	public static class FontAndColorProvider extends
			AdapterFactoryLabelProvider implements IColorProvider,
			IFontProvider, ITableColorProvider, ITableFontProvider {
		/**
		 * Construct an instance that wraps the given factory and specifies the
		 * given default font and colors.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param defaultFont
		 *            the font that will be used when no font is specified.
		 * @param defaultForeground
		 *            the foreground color that will be used when no foreground
		 *            color is specified.
		 * @param defaultBackground
		 *            the background color that will be used when no background
		 *            color is specified.
		 */
		public FontAndColorProvider(IAdapterFactory adapterFactory,
				Font defaultFont, Color defaultForeground,
				Color defaultBackground) {
			super(adapterFactory);
			setDefaultFont(defaultFont);
			setDefaultForeground(defaultForeground);
			setDefaultBackground(defaultBackground);
		}

		/**
		 * Construct an instance that wraps the given factory and uses the font
		 * and colors of the viewer's control.
		 * 
		 * @param adapterFactory
		 *            an adapter factory that yield adapters that implement the
		 *            various item label provider interfaces.
		 * @param viewer
		 *            the viewer for which the control's font and color should
		 *            be used.
		 */
		public FontAndColorProvider(IAdapterFactory adapterFactory,
				Viewer viewer) {
			this(adapterFactory, viewer.getControl().getFont(), viewer
					.getControl().getForeground(), viewer.getControl()
					.getBackground());
		}
	}

	/**
	 * This keep track of the one factory we are using. Use a
	 * {@link net.enilink.komma.edit.provider.ComposedAdapterFactory} if
	 * adapters from more the one factory are involved in the model.
	 */
	protected IAdapterFactory adapterFactory;

	/**
	 * The font that will be used when no font is specified.
	 */
	protected Font defaultFont;

	/**
	 * The foreground color that will be used when no foreground color is
	 * specified.
	 */
	protected Color defaultForeground;

	/**
	 * The background color that will be used when no background color is
	 * specified.
	 */
	protected Color defaultBackground;

	/**
	 * This keeps track of the label provider listeners.
	 */
	protected Collection<ILabelProviderListener> labelProviderListeners;

	/**
	 * Whether label update notifications are fired.
	 */
	protected boolean isFireLabelUpdateNotifications;

	private static final Class<?> IItemLabelProviderClass = IItemLabelProvider.class;
	private static final Class<?> ITableItemLabelProviderClass = ITableItemLabelProvider.class;
	private static final Class<?> IItemFontProviderClass = IItemFontProvider.class;
	private static final Class<?> IItemColorProviderClass = IItemColorProvider.class;
	private static final Class<?> ITableItemFontProviderClass = ITableItemFontProvider.class;
	private static final Class<?> ITableItemColorProviderClass = ITableItemColorProvider.class;

	/**
	 * Construct an instance that wraps the given factory. If the adapter
	 * factory is an {@link IChangeNotifier}, a listener is added to it, so it's
	 * important to call {@link #dispose()}.
	 * 
	 * @param adapterFactory
	 *            an adapter factory that yield adapters that implement the
	 *            various item label provider interfaces.
	 */
	@SuppressWarnings("unchecked")
	public AdapterFactoryLabelProvider(IAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
		if (adapterFactory instanceof INotifier) {
			((INotifier<INotification>) adapterFactory).addListener(this);
		}

		labelProviderListeners = new ArrayList<ILabelProviderListener>();
	}

	/**
	 * Return the wrapped AdapterFactory.
	 */
	public IAdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	/**
	 * Set the wrapped AdapterFactory. If the adapter factory is an
	 * {@link IChangeNotifier}, a listener is added to it, so it's important to
	 * call {@link #dispose()}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setAdapterFactory(IAdapterFactory adapterFactory) {
		if (this.adapterFactory instanceof INotifier) {
			((INotifier) this.adapterFactory).removeListener(this);
		}

		if (adapterFactory instanceof INotifier) {
			((INotifier) adapterFactory).addListener(this);
		}

		this.adapterFactory = adapterFactory;
	}

	/**
	 * Return the default font.
	 */
	public Font getDefaultFont() {
		return defaultFont;
	}

	/**
	 * Set the default font.
	 */
	public void setDefaultFont(Font font) {
		defaultFont = font;
	}

	/**
	 * Return the default foreground color.
	 */
	public Color getDefaultForeground() {
		return defaultForeground;
	}

	/**
	 * Set the default foreground color.
	 */
	public void setDefaultForeground(Color color) {
		defaultForeground = color;
	}

	/**
	 * Return the default background color.
	 */
	public Color getDefaultBackground() {
		return defaultBackground;
	}

	/**
	 * Set the default background color.
	 */
	public void setDefaultBackground(Color color) {
		defaultBackground = color;
	}

	/**
	 * Since we won't ever generate these notifications, we can just ignore
	 * this.
	 */
	public void addListener(ILabelProviderListener listener) {
		labelProviderListeners.add(listener);
	}

	/**
	 * Since we won't ever add listeners, we can just ignore this.
	 */
	public void removeListener(ILabelProviderListener listener) {
		labelProviderListeners.remove(listener);
	}

	/**
	 * This discards the content provider and removes this as a listener to the
	 * {@link #adapterFactory}.
	 */
	@SuppressWarnings("unchecked")
	public void dispose() {
		if (this.adapterFactory instanceof INotifier) {
			((INotifier<INotification>) adapterFactory).removeListener(this);
		}
	}

	/**
	 * This always returns true right now.
	 */
	public boolean isLabelProperty(Object object, String id) {
		return true;
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ILabelProvider}.getImage
	 * by forwarding it to an object that implements
	 * {@link net.enilink.komma.edit.provider.IItemLabelProvider#getImage
	 * IItemLabelProvider.getImage}
	 */
	public Image getImage(Object object) {
		// Get the adapter from the factory.
		IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory
				.adapt(object, IItemLabelProviderClass);

		return itemLabelProvider != null ? getImageFromObject(itemLabelProvider
				.getImage(object)) : getDefaultImage(object);
	}

	protected Image getDefaultImage(Object object) {
		String image = "full/obj16/GenericValue";
		if (object instanceof String) {
			image = "full/obj16/TextValue";
		} else if (object instanceof Boolean) {
			image = "full/obj16/BooleanValue";
		} else if (object instanceof Float || object instanceof Double) {
			image = "full/obj16/RealValue";
		} else if (object instanceof Integer || object instanceof Short
				|| object instanceof Long || object instanceof Byte) {
			image = "full/obj16/RealValue";
		}

		return getImageFromObject(KommaEditPlugin.INSTANCE.getImage(image));
	}

	protected Image getImageFromObject(Object object) {
		return ExtendedImageRegistry.getInstance().getImage(object);
	}

	/**
	 * This implements {@link ILabelProvider}.getText by forwarding it to an
	 * object that implements {@link IItemLabelProvider#getText
	 * IItemLabelProvider.getText}
	 */
	public String getText(Object object) {
		// Get the adapter from the factory.
		IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory
				.adapt(object, IItemLabelProviderClass);

		return itemLabelProvider != null ? itemLabelProvider.getText(object)
				: object == null ? "" : object.toString();
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.IFontProvider}.getFont
	 * by forwarding it to an object that implements
	 * {@link net.enilink.komma.edit.provider.IItemFontProvider#getFont
	 * IItemFontProvider.getFont}
	 */
	public Font getFont(Object object) {
		// Get the adapter from the factory.
		IItemFontProvider itemFontProvider = (IItemFontProvider) adapterFactory
				.adapt(object, IItemFontProviderClass);

		return itemFontProvider != null ? getFontFromObject(itemFontProvider
				.getFont(object)) : null;
	}

	protected Font getFontFromObject(Object object) {
		return object == null ? null : ExtendedFontRegistry.INSTANCE.getFont(
				defaultFont, object);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.IColorProvider}
	 * .getForeground by forwarding it to an object that implements
	 * {@link net.enilink.komma.edit.provider.IItemColorProvider#getForeground
	 * IItemColorProvider.getForeground}
	 */
	public Color getForeground(Object object) {
		// Get the adapter from the factory.
		IItemColorProvider itemColorProvider = (IItemColorProvider) adapterFactory
				.adapt(object, IItemColorProviderClass);

		return itemColorProvider != null ? getColorFromObject(itemColorProvider
				.getForeground(object)) : null;
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.IColorProvider}
	 * .getBackground by forwarding it to an object that implements
	 * {@link net.enilink.komma.edit.provider.IItemColorProvider#getBackground
	 * IItemColorProvider.getBackground}
	 */
	public Color getBackground(Object object) {
		// Get the adapter from the factory.
		IItemColorProvider itemColorProvider = (IItemColorProvider) adapterFactory
				.adapt(object, IItemColorProviderClass);

		return itemColorProvider != null ? getColorFromObject(itemColorProvider
				.getBackground(object)) : null;
	}

	protected Color getColorFromObject(Object object) {
		return object == null ? null : ExtendedColorRegistry.INSTANCE.getColor(
				defaultForeground, defaultBackground, object);
	}

	/**
	 * This implements {@link ITableLabelProvider}.getColumnImage by forwarding
	 * it to an object that implements
	 * {@link ITableItemLabelProvider#getColumnImage
	 * ITableItemLabelProvider.getColumnImage} or failing that, an object that
	 * implements {@link IItemLabelProvider#getImage
	 * IItemLabelProvider.getImage} where the columnIndex is ignored.
	 */
	public Image getColumnImage(Object object, int columnIndex) {
		// Get the adapter from the factory.
		ITableItemLabelProvider tableItemLabelProvider = (ITableItemLabelProvider) adapterFactory
				.adapt(object, ITableItemLabelProviderClass);

		// No image is a good default.
		Image result = null;

		// Now we could check that the adapter implements interface
		// ITableItemLabelProvider.
		if (tableItemLabelProvider != null) {
			// And delegate the call.
			result = getImageFromObject(tableItemLabelProvider.getColumnImage(
					object, columnIndex));
		}
		// Otherwise, we could check that the adapter implements interface
		// IItemLabelProvider.
		else {
			IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory
					.adapt(object, IItemLabelProviderClass);
			if (itemLabelProvider != null) {
				// And delegate the call.
				result = getImageFromObject(itemLabelProvider.getImage(object));
			}
		}

		return result;
	}

	/**
	 * This implements {@link ITableLabelProvider}.getColumnText by forwarding
	 * it to an object that implements
	 * {@link ITableItemLabelProvider#getColumnText
	 * ITableItemLabelProvider.getColumnText} or failing that, an object that
	 * implements {@link IItemLabelProvider#getText IItemLabelProvider.getText}
	 * where the columnIndex are is ignored.
	 */
	public String getColumnText(Object object, int columnIndex) {
		// Get the adapter from the factory.
		ITableItemLabelProvider tableItemLabelProvider = (ITableItemLabelProvider) adapterFactory
				.adapt(object, ITableItemLabelProviderClass);

		// Now we could check that the adapter implements interface
		// ITableItemLabelProvider.
		if (tableItemLabelProvider != null) {
			// And delegate the call.
			return tableItemLabelProvider.getColumnText(object, columnIndex);
		}
		// Otherwise, we could check that the adapter implements interface
		// IItemLabelProvider.
		else {
			IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory
					.adapt(object, IItemLabelProviderClass);
			if (itemLabelProvider != null) {
				// And delegate the call.
				return itemLabelProvider.getText(object);
			}
			// If there is a column object, just convert it to a string.
			else if (object != null) {
				return object.toString();
			} else {
				return "";
			}
		}
	}

	/**
	 * This implements {@link ITableFontProvider}.getFont by forwarding it to an
	 * object that implements {@link ITableItemFontProvider#getFont
	 * ITableItemFontProvider.getFont} or failing that, an object that
	 * implements {@link IItemFontProvider#getFont IItemFontProvider.getFont}
	 * where the columnIndex is ignored.
	 */
	public Font getFont(Object object, int columnIndex) {
		// Get the adapter from the factory.
		ITableItemFontProvider tableItemFontProvider = (ITableItemFontProvider) adapterFactory
				.adapt(object, ITableItemFontProviderClass);

		// No font is a good default.
		Font result = null;

		// Now we could check that the adapter implements interface
		// ITableItemFontProvider.
		if (tableItemFontProvider != null) {
			// And delegate the call.
			result = getFontFromObject(tableItemFontProvider.getFont(object,
					columnIndex));
		}
		// Otherwise, we could check that the adapter implements interface
		// IItemFontProvider.
		else {
			IItemFontProvider itemFontProvider = (IItemFontProvider) adapterFactory
					.adapt(object, IItemFontProviderClass);
			if (itemFontProvider != null) {
				// And delegate the call.
				result = getFontFromObject(itemFontProvider.getFont(object));
			}
		}

		return result;
	}

	/**
	 * This implements {@link ITableColorProvider}.getForeground by forwarding
	 * it to an object that implements
	 * {@link ITableItemColorProvider#getForeground
	 * ITableItemColorProvider.getForeground} or failing that, an object that
	 * implements {@link IItemColorProvider#getForeground
	 * IItemColorProvider.getForeground} where the columnIndex is ignored.
	 */
	public Color getForeground(Object object, int columnIndex) {
		// Get the adapter from the factory.
		ITableItemColorProvider tableItemColorProvider = (ITableItemColorProvider) adapterFactory
				.adapt(object, ITableItemColorProviderClass);

		// No color is a good default.
		Color result = null;

		// Now we could check that the adapter implements interface
		// ITableItemColorProvider.
		if (tableItemColorProvider != null) {
			// And delegate the call.
			result = getColorFromObject(tableItemColorProvider.getForeground(
					object, columnIndex));
		}
		// Otherwise, we could check that the adapter implements interface
		// IItemColorProvider.
		else {
			IItemColorProvider itemColorProvider = (IItemColorProvider) adapterFactory
					.adapt(object, IItemColorProviderClass);
			if (itemColorProvider != null) {
				// And delegate the call.
				//
				result = getColorFromObject(itemColorProvider
						.getForeground(object));
			}
		}

		return result;
	}

	/**
	 * This implements {@link ITableColorProvider}.getBackground by forwarding
	 * it to an object that implements
	 * {@link ITableItemColorProvider#getBackground
	 * ITableItemColorProvider.getBackground} or failing that, an object that
	 * implements {@link IItemColorProvider#getBackground
	 * IItemColorProvider.getBackground} where the columnIndex is ignored.
	 */
	public Color getBackground(Object object, int columnIndex) {
		// Get the adapter from the factory.
		ITableItemColorProvider tableItemColorProvider = (ITableItemColorProvider) adapterFactory
				.adapt(object, ITableItemColorProviderClass);

		// No color is a good default.
		Color result = null;

		// Now we could check that the adapter implements interface
		// ITableItemColorProvider.
		if (tableItemColorProvider != null) {
			// And delegate the call.
			//
			result = getColorFromObject(tableItemColorProvider.getBackground(
					object, columnIndex));
		}
		// Otherwise, we could check that the adapter implements interface
		// IItemColorProvider.
		else {
			IItemColorProvider itemColorProvider = (IItemColorProvider) adapterFactory
					.adapt(object, IItemColorProviderClass);
			if (itemColorProvider != null) {
				// And delegate the call.
				result = getColorFromObject(itemColorProvider
						.getBackground(object));
			}
		}

		return result;
	}

	/**
	 * Returns whether this label provider fires
	 * {@link ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 * update notifications}.
	 */
	public boolean isFireLabelUpdateNotifications() {
		return isFireLabelUpdateNotifications;
	}

	/**
	 * Sets whether this label provider fires
	 * {@link ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 * update notifications}.
	 */
	public void setFireLabelUpdateNotifications(
			boolean isFireLabelUpdateNotifications) {
		this.isFireLabelUpdateNotifications = isFireLabelUpdateNotifications;
	}

	public void fireLabelProviderChanged() {
		for (ILabelProviderListener labelProviderListener : labelProviderListeners) {
			labelProviderListener
					.labelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	@Override
	public void notifyChanged(Collection<? extends INotification> notifications) {
		if (isFireLabelUpdateNotifications()) {
			for (INotification notification : notifications) {
				if (!(notification instanceof IViewerNotification)
						|| ((IViewerNotification) notification).isLabelUpdate()) {
					for (ILabelProviderListener labelProviderListener : labelProviderListeners) {
						labelProviderListener
								.labelProviderChanged(new LabelProviderChangedEvent(
										this, notification.getSubject()));
					}
				}
			}
		}
	}

	@Override
	public NotificationFilter<INotification> getFilter() {
		return null;
	}
}
