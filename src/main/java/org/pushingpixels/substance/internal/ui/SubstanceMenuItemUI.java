/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.substance.internal.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.TransitionAwareUI;
import org.pushingpixels.substance.internal.utils.*;
import org.pushingpixels.substance.internal.utils.menu.MenuUtilities;
import org.pushingpixels.substance.internal.utils.menu.SubstanceMenu;
import org.pushingpixels.substance.internal.utils.menu.MenuUtilities.MenuPropertyListener;

/**
 * UI for menu items in <b>Substance</b> look and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceMenuItemUI extends BasicMenuItemUI implements
		SubstanceMenu, TransitionAwareUI {
	protected StateTransitionTracker stateTransitionTracker;

	/**
	 * Listens on all changes to the underlying menu item.
	 */
	protected MenuPropertyListener substanceMenuPropertyListener;

	/**
	 * Rollover listener.
	 */
	protected RolloverMenuItemListener substanceRolloverListener;

	protected PropertyChangeListener substancePropertyListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent comp) {
		SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
		return new SubstanceMenuItemUI((JMenuItem) comp);
	}

	public SubstanceMenuItemUI(JMenuItem menuItem) {
		this.stateTransitionTracker = new StateTransitionTracker(menuItem,
				menuItem.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#installListeners()
	 */
	@Override
	protected void installListeners() {
		super.installListeners();

		// Improving performance on big menus.
		this.substanceMenuPropertyListener = new MenuPropertyListener(
				this.menuItem);
		this.substanceMenuPropertyListener.install();

		// fix for defect 109 - storing reference to rollover listener
		this.substanceRolloverListener = new RolloverMenuItemListener(
				this.menuItem, this.stateTransitionTracker);
		this.menuItem.addMouseListener(this.substanceRolloverListener);

		this.stateTransitionTracker.registerModelListeners();

		this.substancePropertyListener = new PropertyChangeListener() {
			@Override
            public void propertyChange(PropertyChangeEvent evt) {
				if (AbstractButton.MODEL_CHANGED_PROPERTY.equals(evt
						.getPropertyName())) {
					stateTransitionTracker.setModel((ButtonModel) evt
							.getNewValue());
				}
				if ("font".equals(evt.getPropertyName())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
                        public void run() {
							if (menuItem != null) {
								menuItem.updateUI();
							}
						}
					});
				}
			}
		};
		this.menuItem.addPropertyChangeListener(this.substancePropertyListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();

		this.defaultTextIconGap = SubstanceSizeUtils
				.getTextIconGap(SubstanceSizeUtils
						.getComponentFontSize(this.menuItem));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#uninstallListeners()
	 */
	@Override
	protected void uninstallListeners() {
		// Improving performance on big menus.
		this.substanceMenuPropertyListener.uninstall();
		this.substanceMenuPropertyListener = null;

		// fix for defect 109 - unregistering rollover listener
		this.menuItem.removeMouseListener(this.substanceRolloverListener);
		this.substanceRolloverListener = null;

		this.stateTransitionTracker.unregisterModelListeners();

		this.menuItem
				.removePropertyChangeListener(this.substancePropertyListener);
		this.substancePropertyListener = null;

		super.uninstallListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pushingpixels.substance.SubstanceMenu#getAssociatedMenuItem()
	 */
	@Override
    public JMenuItem getAssociatedMenuItem() {
		return this.menuItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pushingpixels.substance.SubstanceMenu#getAcceleratorFont()
	 */
	@Override
    public Font getAcceleratorFont() {
		return this.acceleratorFont;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pushingpixels.substance.SubstanceMenu#getArrowIcon()
	 */
	@Override
    public Icon getArrowIcon() {
		return this.arrowIcon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pushingpixels.substance.SubstanceMenu#getCheckIcon()
	 */
	@Override
    public Icon getCheckIcon() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pushingpixels.substance.SubstanceMenu#getDefaultTextIconGap()
	 */
	@Override
    public int getDefaultTextIconGap() {
		return this.defaultTextIconGap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicMenuItemUI#getPreferredMenuItemSize(javax
	 * .swing.JComponent, javax.swing.Icon, javax.swing.Icon, int)
	 */
	@Override
	protected Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon,
			Icon arrowIcon, int defaultTextIconGap) {
		Dimension superDim = super.getPreferredMenuItemSize(c, checkIcon,
				arrowIcon, defaultTextIconGap);
		return new Dimension(MenuUtilities.getPreferredWidth(menuItem),
				superDim.height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicMenuItemUI#paintMenuItem(java.awt.Graphics,
	 * javax.swing.JComponent, javax.swing.Icon, javax.swing.Icon,
	 * java.awt.Color, java.awt.Color, int)
	 */
	@Override
	protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon,
			Icon arrowIcon, Color background, Color foreground,
			int defaultTextIconGap) {
		MenuUtilities.paintMenuItem(g, menuItem, checkIcon, arrowIcon,
				defaultTextIconGap);
	}

	@Override
	public StateTransitionTracker getTransitionTracker() {
		return this.stateTransitionTracker;
	}

	@Override
	public boolean isInside(MouseEvent me) {
		return true;
	}
}
