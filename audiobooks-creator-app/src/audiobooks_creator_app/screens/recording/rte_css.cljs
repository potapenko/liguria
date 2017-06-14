(ns audiobooks-creator-app.screens.recording.rte-css)

;; <style type="text/css">
(def css "
			* {
        -webkit-user-select: none;
				outline: 0px solid transparent;
				-webkit-tap-highlight-color: rgba(0,0,0,0);
				-webkit-touch-callout: none !important;
			}

      s{
        color: rgba(0,0,0,0.2);
      }
      u{
        color: rgba(0,0,0,0.5);
      }
			html, body {
				padding: 0;
				margin: 0;
				font-family: Arial, Helvetica, sans-serif;
				font-size:1em;
				color:#2d4150;
			}

			body {
				padding-left:0px;
				padding-right:0px;
				padding-top: 0px;
				padding-bottom: 0px;
				overflow-y: scroll;
				-webkit-overflow-scrolling: touch;
				height: 100%;
			}

			img.zs_active {
				/*border: 2px dashed #000;*/
			}

			img {
				max-width: 98%;
				margin-left:auto;
				margin-right:auto;
				display: block;
			}

			audio {
				padding: 20px 0;
			}

			div.zss_editor_content {
				font-family: Arial, Helvetica, sans-serif;
				color: #000;
				width: 100%;
				height: 100%;
				-webkit-overflow-scrolling: touch;
				overflow:auto;

			}

			#zss_editor_content {
				padding-left: 0px;
				padding-right: 0px;
			}

			div.zss_editor_title {
				font-family: 'Helvetica Neue Light', sans-serif;
				font-size: 23px;
				line-height: 31px;
				color: #2d4150;
			}

			#zss_editor_title {
				padding-left: 0px;
				padding-right: 0px;
			}

			[placeholder]:empty:before {
				content: attr(placeholder);
				color: #e0e0e0;
			}

			[placeholder]:empty:focus:before {
				content: attr(placeholder);
				color: #e0e0e0;
			}

			hr {
				border: none;
				height: 1px;
				background-color: #e7e7e7;
			}

			#separatorContainer {
				-webkit-user-select: none;
				padding-left: 0px;
				padding-right: 0px;
			}
")
;; </style>
