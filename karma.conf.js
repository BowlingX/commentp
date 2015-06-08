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

module.exports = function (config) {
    config.set({
        basePath: '',
        files: [
            'src/test/js/**/*.js'
        ],
        frameworks: ['jasmine-jquery', 'jasmine'],
        browsers: ['Chrome'],
        preprocessors: {'./src/**/*.js': ['webpack']},
        reporters: ['progress', 'coverage'],
        coverageReporter: {
            type: 'html',
            dir: 'coverage/'
        },
        plugins: [
            'karma-chrome-launcher',
            'karma-jasmine',
            'karma-jasmine-jquery',
            'karma-junit-reporter',
            'karma-coverage',
            require("karma-webpack")
        ],
        webpackMiddleware: {
            // webpack-dev-middleware configuration
            // i. e.
            noInfo: true
        },
        junitReporter: {
            outputFile: 'test_out/unit.xml',
            suite: 'unit'
        },
        webpack: require('./webpack.test.config.js')
    });
};