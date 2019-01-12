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
import org.omidbiz.util.JSFUtil;

public class SuggestionRendererBase extends HeaderResourcesRendererBase
{

    public static final String HIDDEN_COMP = "Id";
    public static final String HIDDEN_NAME_COMP = "Name";

    public static final String SUGGESTION_NAME_PARAM = "forceId";

    InternetResource[] jsResources = { getResource("/org/omidbiz/renderkit/html/script/qtip.js"),
            getResource("/org/omidbiz/renderkit/html/script/suggestionManager.js") };

    InternetResource[] cssResources = { getResource("/org/omidbiz/renderkit/html/css/qtip.css") };

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
    protected void doDecode(FacesContext context, UIComponent component)
    {
        ExternalContext external = context.getExternalContext();
        Map requestParams = external.getRequestParameterMap();
        UISuggestion sugesstion = (UISuggestion) component;
        sugesstion.setClientParametForceId((String) component.getAttributes().get("clientParametForceId"));
        sugesstion.setWidth((Integer) component.getAttributes().get("width"));
        sugesstion.setHeight((Integer) component.getAttributes().get("height"));
        sugesstion.setStyleClass((String) component.getAttributes().get("styleClass"));
        sugesstion.setStyle((String) component.getAttributes().get("style"));
        String componentId = (String) sugesstion.getForceId();
        String hiddenComponentId = componentId + HIDDEN_COMP;
        String valueName = (String) requestParams.get(componentId + HIDDEN_NAME_COMP);
        String valueId = (String) requestParams.get(hiddenComponentId);
        sugesstion.setSubmittedValue(valueName);
        
        if (sugesstion.isRequired() && JSFUtil.isEmpty(valueId))
        {
            sugesstion.setValueName(null);
            sugesstion.setValueId(null);
            sugesstion.setValid(false);
        }
        else
        {

            sugesstion.setValueName(valueName);
            sugesstion.setValueId(valueId);
            sugesstion.setValid(true);
        }
    }

    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {
        String clientId = component.getClientId(context);
        UISuggestion sugesstion = (UISuggestion) component;
        String forceId = (String) sugesstion.getForceId();
        String componentId = clientId.replace(":", "\\\\:");
        componentId = forceId.replace(":", "\\\\:");
        String view = (String) sugesstion.getView();
        encodeInput(writer, context, component, componentId);
        generateScripts(context, component, componentId, view);
    }

    private String generateQueryStrings(UIComponent component, String compId)
    {
        List<UIComponent> children = component.getChildren();
        StringBuilder params = new StringBuilder("?").append(SUGGESTION_NAME_PARAM).append("=").append(compId);
        if (children != null && children.size() > 0)
        {
            for (UIComponent uiComponent : children)
            {
                if (uiComponent instanceof UIParameter)
                {
                    params.append("&");
                    UIParameter param = (UIParameter) uiComponent;
                    // TODO : urlencode
                    params.append(param.getName()).append("=").append(param.getValue());
                }
            }
        }
        return params == null ? "" : params.toString();
    }

    private void generateScripts(FacesContext context, UIComponent component, String componentId, String baseUrl) throws IOException
    {
        String hiddenComponentId = componentId + HIDDEN_COMP;
        StringBuffer script = new StringBuffer();
        UISuggestion sugesstion = (UISuggestion) component;
        script.append(" jQuery(document).ready(function(){");
        if(sugesstion.getClientParametForceId() != null)
            script.append("function setInputClientParameterId_"+componentId+" (){ var inputClientParameterId = '#"+sugesstion.getClientParametForceId()+"'; return '"+sugesstion.getClientParametForceId()+"='+jQuery(inputClientParameterId).val();};");
        script.append("var timer =0;");
        String url = baseUrl + generateQueryStrings(component, componentId)+"&";
        int width = sugesstion.getWidth() == 0 ? 550 : sugesstion.getWidth();
        int height = sugesstion.getHeight() == 0 ? 250 : sugesstion.getHeight();
        if(sugesstion.getClientParametForceId() != null)
            script.append(String.format("sm.createQtip('%s', '%s', %s, %s, setInputClientParameterId_"+componentId+");", componentId, url, width, height));
        else
            script.append(String.format("sm.createQtip('%s', '%s', %s, %s);", componentId, url, width, height));
        script.append(String.format("jQuery(\"#%s\").keyup(function(){", componentId + HIDDEN_NAME_COMP));
        script.append("if (timer) {clearTimeout(timer);}");
        script.append("var par = {};");
        // script.append("var baseUrl ='").append(baseUrl).append("';");
        List<UIComponent> children = component.getChildren();
        if (children != null && children.size() > 0)
        {
            for (UIComponent uiComponent : children)
            {
                if (uiComponent instanceof UIParameter)
                {
                    UIParameter par = (UIParameter) uiComponent;
                    if (par.getValue() instanceof String)
                        script.append("par[\"").append(par.getName()).append("\"]").append("='").append(par.getValue()).append("'; ");
                    else
                        script.append("par[\"").append(par.getName()).append("\"]").append("=").append(par.getValue()).append("; ");
                }
            }
        }
        script.append("par[\"").append(componentId + HIDDEN_NAME_COMP).append("\"]").append("=").append("jQuery(this).val();");
        script.append("par[\"").append(SUGGESTION_NAME_PARAM).append("\"]").append("=\"").append(componentId).append("\"; ");        
        script.append(String.format("timer = setTimeout(sm.searchAndReload, 600, '%s', '%s', par);", componentId, baseUrl + "?"));
        script.append("jQuery('#"+hiddenComponentId+"') != undefined? jQuery('#"+hiddenComponentId+"').val(undefined): undefined;");
        script.append("});});");
        getUtils().writeScript(context, component, script);
    }

    private void encodeInput(ResponseWriter writer, FacesContext context, UIComponent component, String componentId) throws IOException
    {
        // String clientId = component.getClientId(context);
        UISuggestion suggestion = (UISuggestion) component;
        writer.startElement("input", null);
        getUtils().writeAttribute(writer, "type", "text");
        getUtils().writeAttribute(writer, "id", componentId + HIDDEN_NAME_COMP);
        getUtils().writeAttribute(writer, "name", componentId + HIDDEN_NAME_COMP);
        getUtils().writeAttribute(writer, "autocomplete", "off");
        Object styleClass = suggestion.getStyleClass();
        if (styleClass != null)
            getUtils().writeAttribute(writer, "class", String.valueOf(styleClass));
        String style = suggestion.getStyle();
        if (style != null && style.trim().length() > 0)
            getUtils().writeAttribute(writer, "style", style);
        Object valueName = suggestion.getValueName();
        if (valueName != null)
            getUtils().writeAttribute(writer, "value", valueName);
        if (suggestion.getStyleClass() != null)
            getUtils().writeAttribute(writer, "class", suggestion.getStyleClass());
        writer.endElement("input");
        //
        writer.startElement("input", null);
        getUtils().writeAttribute(writer, "type", "hidden");
        getUtils().writeAttribute(writer, "id", componentId + HIDDEN_COMP);
        Object valueId = suggestion.getValueId();
        if (valueId != null)
            getUtils().writeAttribute(writer, "value", valueId);
        getUtils().writeAttribute(writer, "name", componentId + HIDDEN_COMP);
        writer.endElement("input");
    }

    @Override
    protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
    {

    }

    @Override
    protected Class<? extends UIComponent> getComponentClass()
    {
        return UISuggestion.class;
    }

    @Override
    public boolean getRendersChildren()
    {
        return false;
    }

}
