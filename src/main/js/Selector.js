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

import Util from 'flexcss/src/main/util/Util';
import Settings from 'flexcss/src/main/util/Settings';
import EventEmitter from 'wolfy87-eventemitter';

export default class Selector extends EventEmitter {

    /**
     * @param {HTMLElement} node
     * @param {Document} document
     */
    constructor(node, document) {
        super();
        this.node = node;
        this.document = document;
        this.init(node);
    }

    /**
     * @param {Range} range
     * @returns {boolean}
     */
    isValidRange(range) {
        return Util.isPartOfNode(range.commonAncestorContainer, this.node);
    }

    init() {
        // append actions
        const appContainer = this.document.createElement('div');
        appContainer.setAttribute('data-commentp-app', Util.guid());
        appContainer.innerHTML = require('templates/selection-action.html');
        this.document.body.appendChild(appContainer);

        const actionContainer = appContainer.querySelector('[data-commentp-action]');

        const event = Settings.isTouchDevice() ? 'selectionchange' : 'mouseup';

        const clickEvent = 'ontouchend' in this.document ? 'touchend' : 'click';

        this.document.addEventListener(event, () => {
            const selection = this.document.getSelection();
            if (selection.rangeCount > 0) {
                const range = selection.getRangeAt(0),
                    isPartOfNode = this.isValidRange(range);
                if (isPartOfNode && !range.collapsed) {
                    var clientRect = range.getBoundingClientRect();
                    actionContainer.classList.add('open');
                    Util.setupPositionNearby(clientRect, actionContainer, this.document.body, true, true);
                    setTimeout(() => {
                        if (!this.document.getSelection().isCollapsed) {
                            Util.addEventOnce(clickEvent, this.document, (thisEvent, self) => {
                                // skip frame to detect if a selection has been canceled
                                setTimeout(() => {
                                    const thisSelection = this.document.getSelection(),
                                        isCollapsed = thisSelection.isCollapsed;
                                    const notValid = isCollapsed ||
                                        (!isCollapsed && !this.isValidRange(thisSelection.getRangeAt(0)));
                                    if (notValid && !Util.isPartOfNode(thisEvent.target, actionContainer)) {
                                        Util.addEventOnce(Settings.getTransitionEvent(), actionContainer, () => {
                                            if (this.document.getSelection().isCollapsed) {
                                                actionContainer.setAttribute('style', '');
                                            }
                                        });
                                        actionContainer.classList.remove('open');
                                    } else {
                                        Util.addEventOnce(clickEvent, this.document, self);
                                    }
                                }, 0);
                            });
                        }
                    }, 0);
                }
            }
        });
    }
}

