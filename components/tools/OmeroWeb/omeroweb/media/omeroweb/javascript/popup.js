/*
 * Help
 *
 * !/usr/bin/env python
 * 
 * 
 * 
 * Copyright (c) 2008 University of Dundee. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
 * 
 * Version: 1.0
 */


function openHelp() {
        owindow = window.open('/webadmin/help/index.htm', '', config='height=650,width=600,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
        if(!owindow.closed) owindow.focus();
}

function openPopup(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var owindow = window.open(url, '', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
    return false;
}


function popupActivites(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var w = 650;
    var h = 400;
    var left = parseInt((screen.availWidth/2) - (w/2));
    var top = parseInt((screen.availHeight - h)/3);
    var activitiesWindow = window.open(url, 'Activities', config='height='+h+',width='+w+',left='+left+',top='+top+',toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!activitiesWindow.closed) activitiesWindow.focus();
    return activitiesWindow;
}


function openCenteredWindow(url) {
    var width = 550;
    var height = 600;
    var left = parseInt((screen.availWidth/2) - (width/2));
    var top = 0 // parseInt((screen.availHeight/2) - (height/2));
    var windowFeatures = "width=" + width + ",height=" + height + ",status=no,resizable=yes,scrollbars=yes,menubar=no,toolbar=no,left=" + left + ",top=" + top + "screenX=" + left + ",screenY=" + top;
    var myWindow = window.open(url, "", windowFeatures);
    if(!myWindow.closed) myWindow.focus();
    return false;
}