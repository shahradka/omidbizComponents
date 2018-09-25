package org.omidbiz.renderkit.html;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
import org.ajax4jsf.resource.InternetResource;
import org.omidbiz.component.UISuggestion;
import org.omidbiz.component.UISuggestionButton;
import org.omidbiz.util.JSFUtil;

public class SuggestionButtonRendererBase extends HeaderResourcesRendererBase
{

    InternetResource[] jsResources = { getResource("/org/omidbiz/renderkit/html/script/suggestionManager.js") };

    @Override
    protected InternetResource[] getScripts()
    {
        return jsResources;
    }

    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {
        String clientId = component.getClientId(context);
        UISuggestionButton sbutton = (UISuggestionButton) component;
        String forceId = (String) sbutton.getForceId();
        if (forceId == null)
            throw new IllegalArgumentException("suggestion button force id can not be empty");
        String jsFuncName = "closeSuggestion_" + forceId;
        //
        StringBuilder onclick = new StringBuilder(jsFuncName).append("(");
        Object valueId = sbutton.getValueId();
        if (valueId != null)
            onclick.append(valueId).append(",");
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("jQuery(document).ready(function(){");
            sb.append(String
                    .format("jQuery('#%s', window.parent.document).remove();", forceId + SuggestionRendererBase.HIDDEN_COMP));
            sb.append("});");
            getUtils().writeScript(context, component, sb.toString());
        }
        Object valueName = sbutton.getValueName();
        if (valueName instanceof String)
            onclick.append("'").append(valueName).append("'");
        else
            onclick.append(valueName);
        Object onSelectCallback = sbutton.getOnSelectCallback();
        if(onSelectCallback instanceof String)
            onclick.append(",").append(onSelectCallback);
        onclick.append(")");
        String spanId = component.getClientId(context);
        writer.startElement("span", null);
        getUtils().writeAttribute(writer, "onclick", onclick.toString());
        getUtils().writeAttribute(writer, "id", spanId);
        getUtils().writeAttribute(writer, "style", "cursor:pointer;");
        String styleClass = sbutton.getStyleClass();
        if (styleClass != null && styleClass.trim().length() > 0)
            getUtils().writeAttribute(writer, "class", styleClass);
        String txt = (String) sbutton.getTextMessage();
        if (JSFUtil.isNotEmpty(txt))
        {
            if (txt instanceof String)
            {
                txt = ((String) txt).replaceAll("<", " &lt; ");
                txt = ((String) txt).replaceAll(">", " &gt; ");
                txt = ((String) txt).replaceAll("\"", " &quot;");
                txt = ((String) txt).replaceAll("&", " &amp; ");
            }            
            writer.write(txt);
        }
        else
            writer.write("select");
        writer.endElement("span");
        StringBuilder script = new StringBuilder();
        script.append("function ").append(jsFuncName).append("(");
        if (valueId != null)
            script.append("valueId").append(",");
        script.append("valueName");
        if(onSelectCallback !=null)
            script.append(",").append("onSelectCallback");
        script.append(")");
        script.append("{").append(
                String.format("jQuery('#%s', window.parent.document).val(valueName);", forceId + SuggestionRendererBase.HIDDEN_NAME_COMP));
        if (valueId != null)
            script.append(String
                    .format("jQuery('#%s', window.parent.document).val(valueId);", forceId + SuggestionRendererBase.HIDDEN_COMP));
        //
        if (sbutton.isCloseOnSelect())
        {
            script.append("var qtipNum = jQuery('#" + forceId.replace(":", "\\\\:") + SuggestionRendererBase.HIDDEN_NAME_COMP
                    + "',window.parent.document).data('hasqtip'); jQuery('#qtip-'+qtipNum,window.parent.document).hide();");
        }
        script.append("if(typeof onSelectCallback == 'function'){onSelectCallback(this,'"+ forceId + SuggestionRendererBase.HIDDEN_NAME_COMP+"')}");
        script.append("};");
        //

        getUtils().writeScript(context, component, script.toString());

    }

    @Override
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {

    }

    @Override
    protected Class<? extends UIComponent> getComponentClass()
    {
        return UISuggestionButton.class;
    }

    @Override
    public boolean getRendersChildren()
    {
        return false;
    }

}
