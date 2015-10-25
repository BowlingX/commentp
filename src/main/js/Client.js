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

export const EVENT_MESSAGE = 'commentp.message';

import ReconnectingWebSocket from 'ReconnectingWebSocket';
import EventEmitter from 'wolfy87-eventemitter';
import {ArrayObserver} from 'observe-js';

/**
 * @type {string}
 */
const PING_FRAME = 'X';

/**
 * @type {string}
 */
const ERROR_FRAME = '!';


/**
 * Basic WebSocket Client that confirms to the `commentp` Protocol
 */
export class Client extends EventEmitter{
    /**
     * @param {string} channelId
     * @param {object} options
     */
    constructor(channelId, options) {
        super();
        this.channelId = channelId;
        this.currentRequestId = 0;
        this.actionResponses = [];

        this.options = {
            // timeout for actions
            requestTimeout: 10000,
            // base webSocket URI
            baseUrl: (process.env.NODE_ENV === 'production') ?
                'ws://commentp.com/sock/sub/' : 'ws://localhost:8080/sock/sub/'
        };

        Object.assign(this.options, options);
    }

    /**
     * Factory to create a new Client connection
     * @param {string} channelId
     * @param {object} [options]
     * @returns {Promise}
     */
    static connect(channelId, options) {
        const client = new Client(channelId, options);
        client.promise = new Promise(resolve => {
            const connection = new ReconnectingWebSocket(client.options.baseUrl + channelId, []);
            client._connection = connection;
            connection.onmessage = (event) => {
                const msg = event.data;
                if (msg && msg !== PING_FRAME && msg !== ERROR_FRAME) {
                    try {
                        const result = JSON.parse(msg);
                        if (result && result.id) {
                            client.actionResponses.push(result);
                            global.Platform.performMicrotaskCheckpoint();
                        } else if (result) {
                            client.emit(EVENT_MESSAGE, result);
                        }
                    } catch (e) {
                        console.error("Could not parse JSON response", e);
                        throw e;
                    }
                }
            };
            // connect and resolve when finished
            connection.onopen = (e) => {
                if (!e.isReconnect) {
                    resolve(client);
                    console.info(`did connect to server with channel-id: ${channelId}`);
                }
            };

        });

        return client.promise;
    }

    /**
     * Runs an action with given parameters
     * @param name
     * @param params
     * @returns {Promise}
     */
    action(name, params) {
        const self = this, currentId = ++this.currentRequestId,
            action = JSON.stringify({
                action: name,
                id: currentId,
                params: params
            });
        const promise = new Promise((resolve, reject) => {
            var resultFound = false;
            if (this._connection.readyState !== 1) {
                reject('WebSocket not connected');
            }

            const observer = new ArrayObserver(self.actionResponses);
            let timeout;
            observer.open((splices) => {
                splices.forEach((change) => {
                    const maybeResult = self.actionResponses[change.index];
                    if (maybeResult.id && parseInt(maybeResult.id) === currentId) {
                        resultFound = true;
                        observer.close();
                        console.info(`got action response for id: ${currentId}`);
                        clearTimeout(timeout);
                        resolve(maybeResult);
                    }
                });
            });
            timeout = setTimeout(function () {
                if (!resultFound) {
                    reject(`Got timeout for id: ${currentId}`);
                    if (observer) {
                        observer.close();
                    }
                }
            }, this.options.requestTimeout);
        });

        this._connection.send(action);
        return promise;
    }

    /**
     * Close connection with client
     */
    close() {
        this._connection.close();
    }
}
