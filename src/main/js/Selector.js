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


        let currentFocusEvent;

        var self = this;

        this.document.addEventListener(event, () => {

            const selection = self.document.getSelection();

            if (!selection.isCollapsed) {
                const range = selection.getRangeAt(0),
                    isPartOfNode = self.isValidRange(range);
                if (isPartOfNode) {
                    var clientRect = range.getBoundingClientRect();

                    Util.setupPositionNearby(clientRect, actionContainer, self.document.body, true, true);

                    if (currentFocusEvent) {
                        input.removeEventListener('focus', currentFocusEvent);
                    }

                    currentFocusEvent = Util.addEventOnce('focus', input, () => {
                        if (self.isValidRange(range)) {
                            self.resetRendering();
                            const renderer = new Marklib.Rendering(self.document);
                            renderer.renderWithRange(range);
                            self.document.getSelection().removeAllRanges();
                            self.currentRendering = renderer;
                        }
                    });

                    if (!actionContainer.classList.contains(CLASS_OPEN)) {
                        setTimeout(() => {
                            actionContainer.classList.add(CLASS_OPEN);

                            Util.addEventOnce(clickEvent, self.document, (e, selfFunction) => {
                                setTimeout(() => {
                                    if (self.document.getSelection().isCollapsed && !Util.isPartOfNode(e.target, actionContainer)) {
                                        this.emit(EVENT_CLOSE, actionContainer, e);
                                        form.reset();
                                        this.resetRendering();
                                        actionContainer.classList.remove(CLASS_OPEN);
                                    } else {
                                        Util.addEventOnce(clickEvent, self.document, selfFunction);
                                    }
                                }, 0);
                            });

                        }, 0);
                    }
                } else {
                    actionContainer.classList.remove(CLASS_OPEN);
                }
            }
        });
    }
}

