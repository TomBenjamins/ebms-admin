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
package nl.clockwork.ebms.admin.web.message;

import java.util.Date;

import nl.clockwork.ebms.Constants.EbMSEventStatus;
import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.admin.Constants;
import nl.clockwork.ebms.admin.dao.EbMSDAO;
import nl.clockwork.ebms.admin.model.EbMSEvent;
import nl.clockwork.ebms.admin.model.EbMSMessage;
import nl.clockwork.ebms.admin.web.BasePage;
import nl.clockwork.ebms.admin.web.BootstrapFeedbackPanel;
import nl.clockwork.ebms.admin.web.MessageProvider;
import nl.clockwork.ebms.admin.web.Utils;
import nl.clockwork.ebms.admin.web.WicketApplication;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MessagePageX extends BasePage
{
	private static final long serialVersionUID = 1L;
	@SpringBean(name="ebMSAdminDAO")
	private EbMSDAO ebMSDAO;
	protected boolean showContent;
	protected Panel messageViewPanel;
	protected boolean rawOutput;

	public MessagePageX(final EbMSMessage message, final WebPage responsePage)
	{
		add(new BootstrapFeedbackPanel("feedback"));
		add(new Label("messageId",message.getMessageId()));
		add(new Label("messageNr",message.getMessageNr()));
		add(new Label("conversationId",message.getConversationId()));
		Link<Void> link = new Link<Void>("viewRefToMessageId")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				setResponsePage(new MessagePageX(ebMSDAO.findMessage(message.getRefToMessageId()),MessagePageX.this));
			}
		};
		link.add(new Label("refToMessageId",message.getRefToMessageId()));
		add(link);
		add(DateLabel.forDatePattern("timestamp",new Model<Date>(message.getTimestamp()),Constants.DATETIME_FORMAT));
		add(new Label("cpaId",message.getCpaId()));
		add(new Label("fromRole",message.getFromRole()));
		add(new Label("toRole",message.getToRole()));
		add(new Label("service",message.getService()));
		add(new Label("action",message.getAction()));
		AjaxLink<Void> linkMessageError = new AjaxLink<Void>("viewMessageError")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				setResponsePage(new MessagePageX(ebMSDAO.findResponseMessage(message.getMessageId()),MessagePageX.this));
			}
		};
		linkMessageError.setEnabled(EbMSMessageStatus.DELIVERY_ERROR.equals(message.getStatus()));
		linkMessageError.add(AttributeModifier.replace("class",Model.of(Utils.getTableCellCssClass(message.getStatus()))));
		linkMessageError.add(new Label("status",message.getStatus()));
		add(linkMessageError);
		add(new Label("statusTime",message.getStatusTime()));
		
		PropertyListView<EbMSEvent> events = 
			new PropertyListView<EbMSEvent>("events",message.getEvents())
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<EbMSEvent> item)
				{
					final ModalWindow errorMessageModalWindow = new ErrorMessageModalWindow("errorMessageWindow",item.getModelObject().getErrorMessage());
					item.add(errorMessageModalWindow);
					item.add(DateLabel.forDatePattern("time",new Model<Date>(item.getModelObject().getTime()),Constants.DATETIME_FORMAT));
					item.add(new Label("type"));
					AjaxLink<Void> link = new AjaxLink<Void>("showErrorMessageWindow")
					{
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(AjaxRequestTarget target)
						{
							errorMessageModalWindow.show(target);
						}
					};
					link.setEnabled(EbMSEventStatus.FAILED.equals(item.getModelObject().getStatus()));
					link.add(new Label("status"));
					item.add(link);
					item.add(DateLabel.forDatePattern("statusTime",new Model<Date>(item.getModelObject().getStatusTime()),Constants.DATETIME_FORMAT));
					item.add(new Label("uri"));
				}
			}
		;
		add(events);

		WebMarkupContainer rawOutputContainer = new WebMarkupContainer("rawOutputContainer")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible()
			{
				return WicketApplication.get().getMessageViewPanels().containsKey(MessageProvider.createId(message.getService(),message.getAction()));
			}
		};
		add(rawOutputContainer);
		
		CheckBox rawOutput = new CheckBox("rawOutput",new PropertyModel<Boolean>(this,"rawOutput"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<String> getLabel()
			{
				return Model.of(getLocalizer().getString("lbl.rawOutput",MessagePageX.this));
			}
		};
		rawOutputContainer.add(rawOutput);

		rawOutput.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				if (getRawOutput())
					messageViewPanel.replaceWith(messageViewPanel = new AttachmentsPanel("attachments",message.getAttachments()));
				else
				{
					try
					{
						messageViewPanel.replaceWith(messageViewPanel = WicketApplication.get().getMessageViewPanels().get(MessageProvider.createId(message.getService(),message.getAction())).getPanel("attachments",message.getAttachments()));
					}
					catch (Exception e)
					{
						warn("Unable to view message for action" + MessageProvider.createId(message.getService(),message.getAction()) + ". " + e.getMessage());
						messageViewPanel.replaceWith(messageViewPanel = new AttachmentsPanel("attachments",message.getAttachments()));
					}
				}
				target.add(getPage());
			}
		});

		if (WicketApplication.get().getMessageViewPanels().containsKey(MessageProvider.createId(message.getService(),message.getAction())))
		{
			try
			{
				add(messageViewPanel = WicketApplication.get().getMessageViewPanels().get(MessageProvider.createId(message.getService(),message.getAction())).getPanel("attachments",message.getAttachments()));
			}
			catch (Exception e)
			{
				warn("Unable to view message for action" + MessageProvider.createId(message.getService(),message.getAction()) + ". " + e.getMessage());
				add(messageViewPanel = new AttachmentsPanel("attachments",message.getAttachments()));
			}

		}
		else
			add(messageViewPanel = new AttachmentsPanel("attachments",message.getAttachments()));
		
		add(new Link<Void>("back")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				setResponsePage(responsePage);
			}
		});

		add(new DownloadEbMSMessageLink("download",ebMSDAO,message));

		final TextArea<String> content = new TextArea<String>("content",Model.of(message.getContent()))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible()
			{
				return showContent;
			}
		};
		content.setOutputMarkupPlaceholderTag(true);
		content.setEnabled(false);
		add(content);

		AjaxLink<String> toggleContent = new AjaxLink<String>("toggleContent")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				showContent = !showContent;
				target.add(this);
				target.add(content);
			}
		};
		toggleContent.add(new Label("label",new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return MessagePageX.this.getLocalizer().getString(showContent ? "cmd.hide" : "cmd.show",MessagePageX.this);
			}
		}));
		add(toggleContent);
	}
	
	@Override
	public String getPageTitle()
	{
		return getLocalizer().getString("message",this);
	}
	
	public boolean getShowContent()
	{
		return showContent;
	}
	
	public boolean getRawOutput()
	{
		return rawOutput;
	}
	
	public void setRawOutput(boolean rawOutput)
	{
		this.rawOutput = rawOutput;
	}
	
	public class ErrorMessageModalWindow extends ModalWindow
	{
		private static final long serialVersionUID = 1L;

		public ErrorMessageModalWindow(String id, String errorMessage)
		{
			super(id);
			setCssClassName(ModalWindow.CSS_CLASS_GRAY);
			setContent(new ErrorMessagePanel(this,Model.of(errorMessage)));
			setCookieName("eventError");
			setCloseButtonCallback(new ModalWindow.CloseButtonCallback()
			{
				private static final long serialVersionUID = 1L;

				public boolean onCloseButtonClicked(AjaxRequestTarget target)
				{
					return true;
				}
			});
		}
		
		@Override
		public IModel<String> getTitle()
		{
			return new Model<String>(getLocalizer().getString("eventError",this));
		}
	}

//	@Override
//	public void renderHead(IHeaderResponse response)
//	{
//		super.renderHead(response);
//		response.render(CssReferenceHeaderItem.forReference(new TextTemplateResourceReference(this.getClass(),"style.css","text/css",new LoadableDetachableModel<Map<String,Object>>()
//		{
//			private static final long serialVersionUID = 1L;
//
//			public Map<String,Object> load()
//			{
//				final Map<String,Object> vars = new HashMap<String,Object>();
//				vars.put("showContent",MessagePage.this.getLocalizer().getString("cmd.show",MessagePage.this));
//				vars.put("hide",MessagePage.this.getLocalizer().getString("cmd.hide",MessagePage.this));
//				return vars;
//			}
//		}),"screen"));
//	}
}
