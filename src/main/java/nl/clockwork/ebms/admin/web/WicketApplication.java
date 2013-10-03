/**
 * Copyright 2013 Clockwork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.clockwork.ebms.admin.web;

import java.util.ArrayList;
import java.util.List;

import nl.clockwork.ebms.admin.web.cpa.CPAsPage;
import nl.clockwork.ebms.admin.web.menu.MenuItem;
import nl.clockwork.ebms.admin.web.menu.MenuLinkItem;
import nl.clockwork.ebms.admin.web.message.MessagesPage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see nl.clockwork.ebms.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	public List<MenuItem> menu = new ArrayList<MenuItem>();
	
	public WicketApplication()
	{
		//MenuItem home = new MenuLinkItem("0","Home",HomePage.class);
		//menu.add(home);
		
		MenuItem cpa = new MenuItem("1","cpaService");
		new MenuLinkItem(cpa,"1","cpas",HomePage.class);
		menu.add(cpa);

		MenuItem message = new MenuItem("2","messageService");
		new MenuLinkItem(message,"1","traffic",HomePage.class);
		new MenuLinkItem(message,"2","new",HomePage.class);
		menu.add(message);

		MenuItem advanced = new MenuItem("3","advanced");
		new MenuLinkItem(advanced,"1","cpas",CPAsPage.class);
		new MenuLinkItem(advanced,"2","messages",MessagesPage.class);
		menu.add(advanced);
	}
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
	}
	
	public MenuItem getMenuItem(String id)
	{
		return findMenuItem(menu,id);
	}

	private static MenuItem findMenuItem(List<MenuItem> menuItems, String id)
	{
		for (MenuItem menuItem : menuItems)
		{
			if (menuItem.getId().equals(id))
				return menuItem;
			MenuItem item = findMenuItem(menuItem.getChildren(),id);
			if (item != null)
				return item;
		}
		return null;
	}

	public static WicketApplication get()
	{
		return (WicketApplication)WebApplication.get();
	}
}
