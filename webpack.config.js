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

var path = require("path");
var webpack = require("webpack"), fs = require('fs');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var isProduction = "production" === process.env.NODE_ENV;
module.exports = {
    watch: false,
    devtool: isProduction ? 'source-map' : 'eval',
    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                include: [
                    path.resolve(__dirname, "src/main/js"),
                    fs.realpathSync(path.resolve(__dirname, "node_modules/flexcss/src/main"))
                ],
                loader: 'babel-loader'
            },
            {
                test: /\.scss$/,
                loader:
                    // activate source maps via loader query
                    'style-loader!css?sourceMap!' +
                    'autoprefixer?browsers=last 2 versions!' +
                    'sass?outputStyle=expanded&sourceMap=true&sourceMapContents=true'

            },
            {
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                loader: 'file-loader?limit=100000'
            },
            {
                test: /\.html/,
                loader:'html-loader'
            }
        ],
        preLoaders: [
            {
                test: /\.js$/,
                exclude: /node_modules/, // exclude any and all files in the node_modules folder
                loader: "eslint-loader"
            }
        ]
    },
    resolve: {
        modulesDirectories: ['src/main/js', 'src/main', 'node_modules']
    },
    entry: {
        'app': ['App']
    },
    output: {
        path: __dirname + "/src/main/webapp/static/build/",
        filename: 'js/[name].min.js',
        libraryTarget: 'umd',
        library: 'Commentp',
        sourceMapFilename: 'js/[name].min.map'
    },
    plugins: [
        new webpack.EnvironmentPlugin(['NODE_ENV']),
        new webpack.ResolverPlugin(
            new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin("bower.json", ["main"])
        )
    ]
};