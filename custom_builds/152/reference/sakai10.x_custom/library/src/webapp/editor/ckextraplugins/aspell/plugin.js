/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msub/rsmart.com/reference/trunk/library/src/webapp/editor/ckextraplugins/aspell/plugin.js $
 * $Id: plugin.js 121459 2013-03-19 18:07:51Z ktakacs@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
/**
 * Aspell plug-in for CKeditor 3.0
 * Ported from FCKeditor 2.x by Christian Boisjoli, SilenceIT
 * Requires toolbar, aspell
 */

CKEDITOR.plugins.add('aspell', {
    init: function (editor) {
        // Create dialog-based command named "aspell"
        editor.addCommand('aspell', new CKEDITOR.dialogCommand('aspell'));

        // Add button to toolbar. Not sure why only that name works for me.
        editor.ui.addButton('SpellCheck', {
            label: editor.lang.wsc.toolbar,
            command: 'aspell',
            icon: 'spellchecker'
        });

        // Add link dialog code
        CKEDITOR.dialog.add('aspell', this.path + 'dialogs/aspell.js');

        // Add CSS
        var aspellCSS = document.createElement('link');
        aspellCSS.setAttribute( 'rel', 'stylesheet');
        aspellCSS.setAttribute('type', 'text/css');
        aspellCSS.setAttribute('href', this.path+'aspell.css');
        document.getElementsByTagName("head")[0].appendChild(aspellCSS);
        delete aspellCSS;
    },
    requires: ['toolbar']
});

