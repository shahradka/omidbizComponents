package org.omidbiz.renderkit.html;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
import org.ajax4jsf.resource.InternetResource;
import org.omidbiz.component.UITabPanel;
import org.omidbiz.component.UITabs;

public class TabsRendererBase extends HeaderResourcesRendererBase
{

    InternetResource[] jsResources = { getResource("/org/omidbiz/renderkit/html/script/tabManager.js"),
            getResource("/org/omidbiz/renderkit/html/script/jquery.ui.core.js"),
            getResource("/org/omidbiz/renderkit/html/script/jquery.ui.widget.js"),
            getResource("/org/omidbiz/renderkit/html/script/jquery.ui.tabs.js") };

    InternetResource[] cssResources = { 
            
            getResource("/org/omidbiz/renderkit/html/css/scrollableTab.css"),
            getResource("org/omidbiz/images/ui-bg_highlight-soft_75_cccccc_1x100.png"),
            getResource("org/omidbiz/images/ui-bg_flat_75_ffffff_40x100.png"),
            getResource("org/omidbiz/images/ui-bg_glass_75_e6e6e6_1x400.png"),
            getResource("org/omidbiz/images/ui-bg_glass_75_dadada_1x400.png"), getResource("org/omidbiz/images/loading.gif"),
            getResource("org/omidbiz/images/ui-bg_glass_65_ffffff_1x400.png") };

    @Override
    protected InternetResource[] getScripts()
    {
        return jsResources;
    }

    @Override
    protected InternetResource[] getStyles()
    {
        return cssResources;
    }

    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {
        String clientId = component.getClientId(context);
        StringBuilder disabledTabs = new StringBuilder();
        StringBuilder js = new StringBuilder("jQuery(document).ready(function(){");
        UITabs tab = ((UITabs) component);

        Boolean verticalTabs = (Boolean) component.getAttributes().get("verticalTabs");
        String jQueryClientId = clientId.replace(":", "\\\\:");
        if(verticalTabs != false)
        {
            js.append("jQuery( '#"+jQueryClientId+"' ).tabs().addClass( 'ui-tabs-vertical ui-helper-clearfix');jQuery( '#"+jQueryClientId+" li' ).removeClass( 'ui-corner-top' ).addClass( 'ui-corner-left' );");
        }        
        js.append("jQuery('#").append(jQueryClientId).append("').tabs({");
        String tabCookieName = "selected-tab_" + jQueryClientId;
        if (tab.isKeepState())
        {
            js.append(" select: function (e, ui) { ");
            js.append(" tabManager.writeCookie('" + tabCookieName + "', ui.index); } ");
        }
        else
        {
            js.append("active: ").append(tab.getActive());
        }
        writer.startElement("div", component);
        getUtils().writeAttribute(writer, "id", clientId);
        // scrollable tab
        writer.startElement("div", null);
        String scrollWidth = (String) tab.getAttributes().get("scrollWidth");
        getUtils().writeAttribute(writer, "style", String.format("width:%s;", scrollWidth));
        if(verticalTabs == false)
        {
            getUtils().writeAttribute(writer, "class", "tab-nav-window");
            
            writer.startElement("div", null);
            getUtils().writeAttribute(writer, "class", "tab-nav-btn tab-nav-next-btn");
            writer.endElement("div");
            
            writer.startElement("div", null);
            getUtils().writeAttribute(writer, "class", "tab-nav-btn tab-nav-prev-btn");
            writer.endElement("div");
        }
        //
        Iterator childIterator = component.getChildren().iterator();
        writer.startElement("ul", component);
        int i = 0;
        boolean addLoading = false;
        boolean hasIframe = false;
        while (childIterator.hasNext())
        {
            UIComponent comp = (UIComponent) childIterator.next();
            if (comp instanceof UITabPanel)
            {
                UITabPanel tp = ((UITabPanel) comp);
                Object labelClass = (Object) tp.getAttributes().get("labelClass");
                if (tp.isRendered())
                {
                    writer.startElement("li", null);
                    String tabIconClass = null;
                    if (labelClass != null)
                    {
                        getUtils().writeAttribute(writer, "class", labelClass);
                    }
                    writer.startElement("a", null);
                    String title = (String) tp.getTitle();
                    String imageSrc = (String) tp.getImageSrc();
                    String imageOnClick = (String) tp.getImageOnClick();
                    String link = (String) tp.getLink();
                    boolean useIframe = tp.isUseIframe();
                    if (tp.isDisabled())
                    {
                        if (i > 0)
                            disabledTabs.append(", ");
                        disabledTabs.append(i);
                    }
                    if (addLoading == false)
                    {
                        if (link != null && link.trim().length() > 0)
                        {
                            // not available on jquery ui 1.8.16
                            // js.append(",")
                            // .append(" beforeLoad: function( event, ui ) {ui.panel.html(\"Loading...\");}");
                            addLoading = true;
                        }
                    }
                    if ((link != null && link.trim().length() > 0) && useIframe == false)
                    {
                        StringBuilder linkWithparams = new StringBuilder(link);
                        if (tp.isIncludePageParams())
                        {
                            addParametersToLink(context, linkWithparams, tp.isJoin());
                        }
                        getUtils().writeAttribute(writer, "href", linkWithparams.toString());
                    }
                    else if ((link != null && link.trim().length() > 0) && useIframe)
                    {
                        StringBuilder linkWithparams = new StringBuilder(link);
                        if (tp.isIncludePageParams())
                        {
                            addParametersToLink(context, linkWithparams, tp.isJoin());
                        }
                        getUtils().writeAttribute(writer, "href", "#tabs-iframe-" + i);
                        getUtils().writeAttribute(writer, "rel", linkWithparams.toString());
                        getUtils().writeAttribute(writer, "class", "iframe-tab");
                        getUtils().writeAttribute(writer, "data-tabindex", i);
                        // register onclick with page load using jquery
                        // getUtils().writeAttribute(writer, "onclick",
                        // "tabManager.loadTabFrame(jQuery(this).attr('href'),jQuery(this).attr('rel'), jQuery(this).data( 'tabindex'));");
                    }
                    else
                    {
                        getUtils().writeAttribute(writer, "href", "#tabs-" + i);
                    }
                    writer.append(title == null ? "" : title);
                    //
                    if (imageSrc != null && imageSrc.trim().length() > 0)
                    {
                        writer.startElement("img", null);
                        getUtils().writeAttribute(writer, "src", imageSrc);
                        getUtils().writeAttribute(writer, "alt", title);
                        if (imageOnClick != null)
                            getUtils().writeAttribute(writer, "onclick", imageOnClick);
                        getUtils().writeAttribute(writer, "class", "tab-image");
                        getUtils().writeAttribute(writer, "style", "cursor:pointer;");
                        writer.endElement("img");
                    }
                    writer.endElement("a");
                    if(tp.getAttributes().get("tabIconClass") !=null)
                    {
                        tabIconClass =  (String) tp.getAttributes().get("tabIconClass");
                        writer.startElement("span", null);
                        getUtils().writeAttribute(writer, "class", tp.getAttributes().get("tabIconClass"));
                        writer.endElement("span");
                        
                    }                    
                    writer.endElement("li");
                }
            }
            i++;
        }
        writer.endElement("ul");
        //
        writer.endElement("div");
        // end of scrollable tab

        Iterator tabIterator = component.getChildren().iterator();
        i = 0;
        while (tabIterator.hasNext())
        {
            UIComponent comp = (UIComponent) tabIterator.next();
            if (comp instanceof UITabPanel)
            {
                UITabPanel tp = ((UITabPanel) comp);
                if (tp.isRendered())
                {
                    if (tp.getLink() == null || String.valueOf(tp.getLink()).trim().length() == 0)
                    {
                        writer.startElement("div", null);
                        getUtils().writeAttribute(writer, "id", "tabs-" + i);
                        int tpCnt = tp.getChildCount();
                        if (tpCnt > 0)
                            renderChildren(context, tp);
                        else
                            writer.append(tp.getContent() == null ? "" : String.valueOf(tp.getContent()));
                        writer.endElement("div");
                    }
                    else
                    {
                        if (tp.isUseIframe())
                        {
                            hasIframe = true;
                            writer.startElement("div", null);
                            getUtils().writeAttribute(writer, "id", "tabs-iframe-" + i);
                            writer.endElement("div");
                        }
                    }
                }
            }
            i++;
        }

        //
        writer.startElement("script", null);
        getUtils().writeAttribute(writer, "type", "text/javascript");
        js.append("});");// tab options
        // responsive tabs
        js.append(String.format(" tabManager.responsiveTabs(%s); ", "jQuery('#" + jQueryClientId + "')"));
        if (tab.isKeepState())
        {
            if (tab.getActive() == 0)
            {
                js.append("jQuery('#").append(jQueryClientId)
                        .append("').tabs('option','selected', parseInt(tabManager.readCookie('" + tabCookieName + "')));\n\r");
            }
            else
            {
                js.append("jQuery('#").append(jQueryClientId)
                        .append("').tabs('option','selected', parseInt(" + tab.getActive() + "));\n\r");
            }
        }
        //
        if (hasIframe)
        {
            js.append(String.format(" var tabIndexActive = jQuery('#%s').tabs('option', 'selected'); ", jQueryClientId));
            js.append(String.format(" var beginTab = jQuery('#%s ul li:eq('+%s+')').find('a'); ", jQueryClientId, "tabIndexActive"));
            js.append(" var dataTabIndex = jQuery(beginTab).data( 'tabindex'); ");
            js.append(" if(jQuery(beginTab).attr('rel')) { ");
            js.append(" tabManager.loadTabFrame(jQuery(beginTab).attr('href'), jQuery(beginTab).attr('rel'), dataTabIndex); ");
            js.append(" } ");
            js.append(" jQuery('a.iframe-tab').click(function() { ");
            js.append(" tabManager.loadTabFrame(jQuery(this).attr('href'),jQuery(this).attr('rel'), jQuery(this).data( 'tabindex')); }); ");
        }
        // $('.tabs-container ul.tabs').tabs('option','disabled', [0, 1,2]);
        if (disabledTabs != null && disabledTabs.length() > 0)
        {
            js.append("jQuery('#").append(jQueryClientId).append("').tabs('option','disabled', [").append(disabledTabs.toString())
                    .append("]);");
        }
        js.append("});");
        writer.write(js.toString());
        writer.endElement("script");
    }

    private void addParametersToLink(FacesContext context, StringBuilder originalLink, boolean joinConversation)
    {
        ExternalContext external = context.getExternalContext();
        Map<String, String> requestParams = external.getRequestParameterMap();
        if (requestParams != null && requestParams.size() > 0)
        {
            if (originalLink.toString().contains("?"))
                originalLink.append("&");
            else
                originalLink.append("?");
            int cnt = 0;
            for (Map.Entry<String, String> entry : requestParams.entrySet())
            {
                if (joinConversation == false)
                {
                    if ("cid".equals(entry.getKey()))
                    {
                        continue;
                    }
                }
                if (cnt > 0)
                    originalLink.append("&");
                originalLink.append(entry.getKey()).append("=").append(entry.getValue());
                cnt++;
            }
        }
    }

    @Override
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {
        writer.endElement("div");
    }

    @Override
    protected Class<? extends UIComponent> getComponentClass()
    {
        return UITabs.class;
    }

    @Override
    public boolean getRendersChildren()
    {
        return false;
    }

}
