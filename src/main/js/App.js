/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 David Heidrich, BowlingX <me@bowlingx.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
'use strict';

require('styles/main.scss');

import {Client, EVENT_MESSAGE} from 'Client';
import Marklib from 'marklib';
import Util from 'flexcss/src/main/util/Util';

const ATTR_COMMENTP = 'data-commentp';

const TIMEOUT = 50;

document.addEventListener('DOMContentLoaded', () => {

    const node = document.querySelector(`[${ATTR_COMMENTP}]`);
    if (node) {
        const channel = node.getAttribute(ATTR_COMMENTP);
        Client.connect(channel).then((client) => {
            let timeout;

            // append actions
            const appContainer = document.createElement('div');
            appContainer.setAttribute('data-commentp-app', Util.guid());
            appContainer.innerHTML = require('templates/selection-action.html');
            document.body.appendChild(appContainer);

            const actionContainer = appContainer.querySelector('[data-commentp-action]');

            const event = 'onmouseup' in document ? 'mouseup' : 'selectionchange';

            const clickEvent = 'ontouchend' in document ? 'touchend' : 'click';
            document.addEventListener(clickEvent, (e) => {
                const selection = document.getSelection();
                if (selection.rangeCount === 0 || selection.toString().trim() === '') {
                    if (!Util.isPartOfNode(e.target, actionContainer)) {
                        actionContainer.classList.remove('open');
                    }
                }
            });

            document.addEventListener(event, () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    const selection = document.getSelection();
                    if (selection.rangeCount > 0) {
                        const range = selection.getRangeAt(0),
                            isPartOfNode = Util.isPartOfNode(range.commonAncestorContainer, node);
                        if (isPartOfNode) {
                            var clientRect = range.getBoundingClientRect();
                            if (clientRect.width > 0) {
                                /*const marking = new Marklib.Rendering(document, 'marking', node);
                                 if (range.startContainer.nodeType === Node.TEXT_NODE
                                 && range.endContainer.nodeType === Node.TEXT_NODE) {
                                 const result = marking.renderWithRange(range);
                                 client.action('mark', result);
                                 selection.removeAllRanges();
                                 }*/

                                actionContainer.classList.add('open');
                                Util.setupPositionNearby(range, actionContainer, document.body, true, true);
                            } else {
                                actionContainer.classList.remove('open');
                            }
                        }
                    } else {
                        actionContainer.classList.remove('open');
                    }
                }, TIMEOUT);
            });

            client.on(EVENT_MESSAGE, (msg) => {
                // FIXME: Create a queue :)
                const renderer = new Marklib.Rendering(document, 'marking-remote', node);
                renderer.renderWithResult(msg);
            });
        });
    }


});

export default Client;
