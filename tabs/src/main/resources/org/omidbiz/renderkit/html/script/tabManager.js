"use strict";
var tabFunc;
var tabManager = {
		writeCookie: function(cname, cvalue){
			document.cookie = cname + "=" + cvalue + "; "
		}, 
		readCookie: function(cname){
			var name = cname + "=";
		    var ca = document.cookie.split(';');
		    for(var i=0; i<ca.length; i++) {
		        var c = ca[i];
		        while (c.charAt(0)==' ') c = c.substring(1);
		        if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
		    }
		    return "";
		},
		loadTabFrame: function(tab, url, uid) {
            if (jQuery(tab).find("iframe").length == 0) {
            	if(typeof tabFunc !== 'undefined')
            	{
            		clearTimeout(tabFunc);
            	}
                var html = [];
                var iframeContent = '<iframe name="_iframeTab_%UID%" id="_iframeTab_%UID%" scrolling="no" onload="javascript:tabManager.onIframeLoadComplete();" frameborder="0" class="ui-tabs-iframe" src="' + url + '">Load Failed?</iframe>';
                html.push('<div class="ui-iframe-holder">');
                html.push(iframeContent.replace(new RegExp('%UID%', 'g'), uid));
                html.push('</div>');
                jQuery(tab).append(html.join(''));
                jQuery(tab).find("iframe").height(jQuery(window).outerHeight()-180);
                tabManager.resizeMonitoring(uid);
            }
            else
            {
	            clearTimeout(tabFunc);
	            tabManager.resizeMonitoring(uid);
            }
            //document.getElementById(frameElm).contentDocument.location.reload();
            var frameId = '_iframeTab_%UID%';
            var frameElm = frameId.replace('%UID%', uid);
            var iframe = document.getElementById(frameElm);
            if(typeof iframe !== 'undefined')
            {
            	//reload iframe with each onclick
            	var tmp_src = iframe.src; 
            	iframe.src = ''; 
            	iframe.src = tmp_src;
            }
            return false;
        },
        onIframeLoadComplete: function(){
        	jQuery(".ui-tabs-iframe").css('background-image', 'none');
        },
        resizeMonitoring:function(uid){
        	if(typeof uid !== 'undefined' || uid != '')
        	{
        		var elmId = '_iframeTab_%UID%';
        		var elementFrame = elmId.replace('%UID%', uid);
        		if(typeof document.getElementById(elementFrame) !== 'undefined')
       			{
        			if(window.frames[elementFrame].document.body !== 'undefined' && window.frames[elementFrame].document.body != null)
        			{
        				if(window.frames[elementFrame].document.body.scrollHeight > (jQuery(window).outerHeight()-180))
        				{
        					document.getElementById(elementFrame).style.height=window.frames[elementFrame].document.body.scrollHeight + 'px';
        				}
        			}
       			}
        	}
        	tabFunc = setTimeout(function(){tabManager.resizeMonitoring(uid)}, 250);
        },
        responsiveTabs:function(elm){
        	var id =elm.attr("id").replace(new RegExp(":"), "\\:");
    		var tabNavWindow=jQuery("#"+id+" .tab-nav-window");
    		var uiTabNav=jQuery("#"+id+" .ui-tabs-nav");
    		var navButtons=jQuery("#"+id+" .tab-nav-btn");
    		var tabs=jQuery("#"+id+" .ui-corner-top");
    		var itemWidths=[];
    		var numberOfTabs=tabs.length;
    		var mainIndex=0;
    		var counter=0;
    		var limitIndex=0;
    		var setLimitIndex=true;
    		var limitIndexTabFinder=0;
    		var limitWidth=tabNavWindow.width();
    		var totalWidthTabs=0;    		
    		tabs.each(function(){    			
    				limitIndexTabFinder++;
    				var tabWidth = jQuery(this).width()+parseInt(jQuery(this).css("margin-right"));
    				itemWidths.push(tabWidth);
    				totalWidthTabs+=tabWidth;    				
    				if(totalWidthTabs>limitWidth && setLimitIndex)
    				{										
    					limitIndex=limitIndexTabFinder;    				
    					setLimitIndex=false;
    				}
    			
    		});
    				
    		if(limitIndex!=0)
    		{				
    			navButtons.show();
    		}
    		// set tab main index for scrolling size
    		mainIndex = numberOfTabs - limitIndex +2;


    		//next btn click
    				navButtons.first().click(function(){
    					if(counter<mainIndex)
    					{
    						navButtons.last().css({"opacity":"1.0","cursor": "pointer"});
    						var shiftForward="+="+(itemWidths[counter]);
    						counter++;
    						uiTabNav.animate({left:shiftForward},500);
    					}
    					if(counter==mainIndex)
    						{
    						    jQuery(this).css({"opacity":"0.2","cursor": "auto"});
    						}
    				}); 
    		//prev btn click
    				navButtons.last().click(function(){
    					if(counter>0)
    					{
    						navButtons.first().css({"opacity":"1.0","cursor": "pointer"});
    						counter--;
    						var shiftbackward='-='+(itemWidths[counter]);
    						uiTabNav.animate({left:shiftbackward},500);
    					}
    					if(counter==0)
    						{
    						    jQuery(this).css({"opacity":"0.2","cursor": "auto"});
    						}
    				});
        }

};  

