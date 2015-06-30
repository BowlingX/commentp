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

var gulp = require('gulp');

// serve all plugins under $ and remove gulp prefix
var $ = require('gulp-load-plugins')({
    replaceString: /^gulp(-|\.)([0-9]+)?/
});

// other Libraries
var
    del = require('del'),
    autoprefixer = require('autoprefixer-core'),
    argv = require('yargs').argv,
    csswring = require('csswring'),
    webpackConfig = require("./webpack.config.js");

var paths = {
    exports: ['src/export.js'],
    tests: ['src/test/**/*.js'],
    images: ['assets/img/**/*', 'themes/img/**/*'],
    fonts: 'assets/fonts/**/*',
    sassThemes: 'src/main/styles/**/*.scss',
    // Karma config file
    karmaConfig: 'karma.conf.js',
    buildPath:'src/main/webapp/static/build/'
};

var onError = function (err) {
    $.util.beep();
    console.log(err);
    // continue:
    this.emit('end');
};

// cleans build directory
gulp.task('clean', function (cb) {
    del(['build'], cb);
});

function createScripts(watch) {
    var config = Object.create(webpackConfig);
    config.watch = watch;
    return gulp.src(paths.exports)
        .pipe($.plumber({
            errorHandler: onError
        }))
        .pipe($.webpack(config))
        .pipe(gulp.dest(paths.buildPath + 'js'))
}

gulp.task('compileScriptsWithDependencies', ['clean'], function () {
    return createScripts(false);
});

gulp.task('watchScriptsWithDependencies', ['clean'], function () {
    return createScripts(true);
});

// setup tests
gulp.task('test', function () {
    return gulp.src('./doesNotExists')
        .pipe($.karma({
            configFile: paths.karmaConfig,
            action: argv.watch ? 'watch' : 'run',
            reporters: argv.writeTestResults ? ['progress', 'junit', 'coverage'] : ['progress', 'coverage']
        }))
        .on('error', function (err) {
            // Make sure failed tests cause gulp to exit non-zero
            $.util.beep();
            throw err;
        });
});


// Copy all static images
gulp.task('images', ['clean'], function () {
    return gulp.start('imagesReload');
});

gulp.task('imagesReload', function () {
    return gulp.src(paths.images)
        // Pass in options to the task
        .pipe($.imagemin({optimizationLevel: 5}))
        .pipe(gulp.dest(paths.buildPath + 'img'));
});

gulp.task('fonts', ['clean'], function () {
    return gulp.src(paths.fonts)
        .pipe(gulp.dest(paths.buildPath + 'fonts'));
});

gulp.task('sass', ['clean'], function () {
    return gulp.start('compileSass');
});

gulp.task('compileSass', function () {
    var processors = [
        autoprefixer({
            browsers: ['last 2 versions'],
            cascade: false
        }),
        csswring
    ];

    return gulp.src(paths.sassThemes)
        .pipe($.plumber({
            errorHandler: onError
        }))
        .pipe($.sourcemaps.init())
        .pipe($.sass())
        .pipe($.postcss(processors))
        .pipe($.sourcemaps.write('.'))
        .pipe(gulp.dest(paths.buildPath + 'css'));
});

// Rerun the task when a file changes
gulp.task('watch', function () {
    // scripts and images
    // sass
    gulp.watch(paths.sassThemes, ['compileSass']);
    gulp.watch(paths.images, ['imagesReload']);

});

// The default task (called when you run `gulp` from cli)
gulp.task('default', ['watch', 'fonts', 'images', 'sass', 'watchScriptsWithDependencies']);

gulp.task('dist', ['fonts', 'images', 'sass', 'compileScriptsWithDependencies']);