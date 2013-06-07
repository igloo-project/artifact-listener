package fr.openwide.maven.artifact.notifier.web.application.navigation.component;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fr.openwide.core.wicket.behavior.ClassAttributeAppender;
import fr.openwide.core.wicket.markup.html.basic.CountLabel;
import fr.openwide.core.wicket.markup.html.basic.HideableLabel;
import fr.openwide.core.wicket.markup.html.panel.GenericPanel;
import fr.openwide.core.wicket.more.markup.html.basic.PlaceholderContainer;
import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.listfilter.ListFilterBehavior;
import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.listfilter.ListFilterOptions;
import fr.openwide.core.wicket.more.markup.html.template.model.NavigationMenuItem;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.model.GenericEntityModel;
import fr.openwide.maven.artifact.notifier.core.business.artifact.model.Artifact;
import fr.openwide.maven.artifact.notifier.core.business.artifact.model.FollowedArtifact;
import fr.openwide.maven.artifact.notifier.core.business.search.model.ArtifactBean;
import fr.openwide.maven.artifact.notifier.core.business.user.service.IUserService;
import fr.openwide.maven.artifact.notifier.core.util.binding.Binding;
import fr.openwide.maven.artifact.notifier.web.application.MavenArtifactNotifierSession;
import fr.openwide.maven.artifact.notifier.web.application.artifact.page.ArtifactDescriptionPage;
import fr.openwide.maven.artifact.notifier.web.application.artifact.page.ArtifactPomSearchPage;
import fr.openwide.maven.artifact.notifier.web.application.artifact.page.ArtifactSearchPage;
import fr.openwide.maven.artifact.notifier.web.application.common.model.EitherModel;
import fr.openwide.maven.artifact.notifier.web.application.navigation.util.LinkUtils;

public class DashboardArtifactPortfolioPanel extends GenericPanel<List<FollowedArtifact>> {

	private static final long serialVersionUID = 6030960404037116497L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardArtifactPortfolioPanel.class);

	@SpringBean
	private IUserService userService;

	public DashboardArtifactPortfolioPanel(String id, IModel<List<FollowedArtifact>> artifactListModel) {
		super(id, artifactListModel);
		
		// Dropdown
		BookmarkablePageLink<Void> followArtifactLink = new BookmarkablePageLink<Void>("followArtifactLink", ArtifactSearchPage.class);
		followArtifactLink.add(new ClassAttributeAppender("dropdown-toggle"));
		followArtifactLink.add(new AttributeAppender("data-toggle", "dropdown"));
		followArtifactLink.add(new AttributeModifier("href", "#"));
		followArtifactLink.add(new Label("followArtifactLabel", new ResourceModel("dashboard.artifact.add")));
		
		WebMarkupContainer caret = new WebMarkupContainer("caret");
		followArtifactLink.add(caret);
		add(followArtifactLink);
		
		WebMarkupContainer dropdownMenu = new ListView<NavigationMenuItem>("dropdownMenu", getSearchDropDownItems()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void populateItem(ListItem<NavigationMenuItem> sousMenuItem) {
				NavigationMenuItem sousMenu = sousMenuItem.getModelObject();
				Class<? extends Page> sousMenuPageClass = sousMenu.getPageClass();
				
				BookmarkablePageLink<Void> navLink = new BookmarkablePageLink<Void>("searchLink", sousMenuPageClass,
						sousMenu.getPageParameters());
				navLink.add(new Label("searchLabel", sousMenu.getLabelModel()));
				
				sousMenuItem.add(navLink);
			}
		};
		add(dropdownMenu);
		
		// List-filter
		ListFilterOptions listFilterOptions = new ListFilterOptions();
		listFilterOptions.setItemsSelector(".artifact");
		listFilterOptions.setScanSelector(".artifact-property");
		
		add(new ListFilterBehavior(listFilterOptions));
		
		// Followed artifacts
		ListView<FollowedArtifact> artifacts = new ListView<FollowedArtifact>("artifacts", artifactListModel) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void populateItem(final ListItem<FollowedArtifact> item) {
				final IModel<FollowedArtifact> followedArtifactModel = item.getModel();
				item.setOutputMarkupId(true);
				
				final IModel<ArtifactBean> backupArtifactBeanModel = new Model<ArtifactBean>(null);
				
				item.add(new ClassAttributeAppender(new EitherModel<String>(new Model<String>(null), new Model<String>("error")) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected boolean shouldGetFirstModel() {
						return followedArtifactModel.getObject() != null;
					}
				}));
				
				// GroupId
				item.add(new Label("artifactGroup", new EitherModel<String>(
						BindingModel.of(followedArtifactModel, Binding.followedArtifact().artifact().group().groupId()),
						BindingModel.of(backupArtifactBeanModel, Binding.artifactBean().groupId())) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected boolean shouldGetFirstModel() {
						return followedArtifactModel.getObject() != null;
					}
				}));
				
				// ArtifactId
				Link<Artifact> artifactIdLink = new BookmarkablePageLink<Artifact>("artifactIdLink", ArtifactDescriptionPage.class,
						LinkUtils.getArtifactPageParameters(followedArtifactModel.getObject().getArtifact()));
				item.add(artifactIdLink);
				artifactIdLink.add(new Label("artifactName", new EitherModel<String>(
						BindingModel.of(followedArtifactModel, Binding.followedArtifact().artifact().artifactId()),
						BindingModel.of(backupArtifactBeanModel, Binding.artifactBean().artifactId())) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected boolean shouldGetFirstModel() {
						return followedArtifactModel.getObject() != null;
					}
				}));
				
				// Rules
				item.add(new CountLabel("rules", "dashboard.artifact.rules",
						BindingModel.of(followedArtifactModel, Binding.followedArtifact().artifactNotificationRules().size())));
				
				// Last version
				HideableLabel lastVersion = new HideableLabel("lastVersion", new EitherModel<String>(
						BindingModel.of(followedArtifactModel, Binding.followedArtifact().artifact().latestVersion().version()),
						BindingModel.of(backupArtifactBeanModel, Binding.artifactBean().latestVersion())) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected boolean shouldGetFirstModel() {
						return followedArtifactModel.getObject() != null;
					}
				});
				item.add(lastVersion);
				item.add(new PlaceholderContainer("noVersions").component(lastVersion));
				
				// Follow / unfollow
				AjaxLink<ArtifactBean> follow = new AjaxLink<ArtifactBean>("follow", backupArtifactBeanModel) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							FollowedArtifact followedArtifact = userService.followArtifactBean(MavenArtifactNotifierSession.get().getUser(), getModelObject());
							backupArtifactBeanModel.setObject(null);
							followedArtifactModel.setObject(followedArtifact);
							target.add(item);
						} catch (Exception e) {
							LOGGER.error("Error occured while following artifact", e);
							Session.get().error(getString("common.error.unexpected"));
						}
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						ArtifactBean artifactBean = getModelObject();
						setVisible(artifactBean != null);
					}
				};
				item.add(follow);
				
				AjaxLink<FollowedArtifact> unfollow = new AjaxLink<FollowedArtifact>("unfollow", followedArtifactModel) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							FollowedArtifact followedArtifact = getModelObject();
							userService.unfollowArtifact(MavenArtifactNotifierSession.get().getUser(), followedArtifact);
							backupArtifactBeanModel.setObject(new ArtifactBean(followedArtifact));
							followedArtifactModel.setObject(null);
							target.add(item);
						} catch (Exception e) {
							LOGGER.error("Error occured while unfollowing artifact", e);
							Session.get().error(getString("common.error.unexpected"));
						}
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						FollowedArtifact followedArtifact = getModelObject();
						setVisible(followedArtifact != null);
					}
				};
				item.add(unfollow);
			}
			
			@Override
			protected IModel<FollowedArtifact> getListItemModel(IModel<? extends List<FollowedArtifact>> listViewModel,
					int index) {
				return new GenericEntityModel<Long, FollowedArtifact>(listViewModel.getObject().get(index));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				List<FollowedArtifact> followedArtifacts = getModelObject();
				setVisible(followedArtifacts != null && !followedArtifacts.isEmpty());
			}
		};
		add(artifacts);
		
		add(new PlaceholderContainer("artifactsPlaceholder").component(artifacts));
	}
	
	private List<NavigationMenuItem> getSearchDropDownItems() {
		List<NavigationMenuItem> searchItems = Lists.newArrayListWithCapacity(2);
		searchItems.add(new NavigationMenuItem(new ResourceModel("navigation.search.pom"), ArtifactPomSearchPage.class));
		searchItems.add(new NavigationMenuItem(new ResourceModel("navigation.search.mavenCentral"), ArtifactSearchPage.class));
		return searchItems;
	}
}