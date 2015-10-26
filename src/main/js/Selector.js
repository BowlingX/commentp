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
import Marklib from 'marklib';

const EVENT_CLOSE = 'close';

const CLASS_OPEN = 'open';

export default class Selector extends EventEmitter {

    /**
     * @param {HTMLElement} node
     * @param {Document} document
     */
    constructor(node, document) {
        super();
        this.node = node;
        this.document = document;
        this.currentRendering = null;
        this.init(node);
    }

    /**
     * @param {Range} range
     * @returns {boolean}
     */
    isValidRange(range) {
        return Util.isPartOfNode(range.commonAncestorContainer, this.node);
    }

    /**
     * Resets a temporary rendering
     */
    resetRendering() {
        if (this.currentRendering) {
            this.currentRendering.destroy();
            delete this.currentRendering;
        }
    }

    init() {
        // append actions
        const appContainer = this.document.createElement('div');
        appContainer.setAttribute('data-commentp-app', Util.guid());
        appContainer.innerHTML = require('templates/selection-action.html');
        this.document.body.appendChild(appContainer);

        const actionContainer = appContainer.querySelector('[data-commentp-action]');
        const input = actionContainer.querySelector('[data-comment-input]'),
            form = actionContainer.getElementsByTagName('form')[0];

        const event = Settings.isTouchDevice() ? 'selectionchange' : 'mouseup';

        const clickEvent = 'ontouchend' in this.document ? 'touchend' : 'click';

        const handleClose = (container, e) => {
            if (container.classList.contains('open') && !Util.isPartOfNode(e.target, container)) {
                container.classList.remove(CLASS_OPEN);
                this.emit(EVENT_CLOSE, container, e);
                this.resetRendering();
                form.reset();
            }
        };

        input.addEventListener('focus', () => {
            const selection = this.document.getSelection();
            if (!selection.isCollapsed && this.isValidRange(selection.getRangeAt(0))) {
                this.resetRendering();
                const renderer = new Marklib.Rendering(this.document);
                renderer.renderWithRange(selection.getRangeAt(0));
                this.document.getSelection().removeAllRanges();
                this.currentRendering = renderer;
            }
        });

        this.document.addEventListener(event, (e) => {
            const selection = this.document.getSelection();
            if (selection.rangeCount > 0) {
                const range = selection.getRangeAt(0),
                    isPartOfNode = this.isValidRange(range);
                if (isPartOfNode && !range.collapsed) {
                    var clientRect = range.getBoundingClientRect();
                    Util.setupPositionNearby(clientRect, actionContainer, this.document.body, true, true);
                    if (!actionContainer.classList.contains(CLASS_OPEN)) {
                        setTimeout(() => {
                            actionContainer.classList.add(CLASS_OPEN);
                            if (!this.document.getSelection().isCollapsed) {
                                Util.addEventOnce(clickEvent, this.document, (thisEvent, self) => {
                                    // skip frame to detect if a selection has been canceled
                                    setTimeout(() => {
                                        const thisSelection = this.document.getSelection(),
                                            isCollapsed = thisSelection.isCollapsed;
                                        const notValid = isCollapsed ||
                                            (!isCollapsed && !this.isValidRange(thisSelection.getRangeAt(0)));
                                        if (notValid) {
                                            handleClose(actionContainer, thisEvent);
                                        } else {
                                            Util.addEventOnce(clickEvent, this.document, self);
                                        }
                                    }, 0);
                                });
                            }
                        }, 0);
                    }
                } else {
                    handleClose(actionContainer, e);
                }
            }
        });
    }
}

