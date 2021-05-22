const path = require('path');
const webpack = require('webpack');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');

// Get manifest
const normalizedPath = require('path').join(__dirname, './target/dependency');
let manifest = '';

require('fs').readdirSync(normalizedPath).forEach(function (file) {
    manifest = `./target/dependency/${file}`;
    console.log(`Sandbox module uses manifest: ${manifest}`);
});

module.exports = (env, argv) => {
    const config = {
        entry: {
            main: [path.resolve(__dirname, 'src/main/javascript/publicPath'), path.resolve(__dirname, 'src/main/javascript/index.js')]
        },
        output: {
            path: path.resolve(__dirname, 'src/main/resources/javascript/apps/'),
            filename: 'jahia.bundle.js',
            chunkFilename: '[name].jahia.[chunkhash:6].js'
        },
        resolve: {
            mainFields: ['module', 'main'],
            extensions: ['.mjs', '.js', '.jsx', 'json']
        },
        optimization: {
            splitChunks: {
                maxSize: 4000000
            }
        },
        module: {
            rules: [
                {
                    test: /\.(js|jsx)$/,
                    include: [path.join(__dirname, 'src')],
                    exclude: /node_modules/,
                    use: {
                        loader: 'babel-loader'
                    }
                },
                {
                    test: /\.s[ac]ss$/i,
                    sideEffects: true,
                    use: [
                        'style-loader',
                        {
                            loader: 'css-loader',
                            options: {
                                modules: {
                                    mode: 'local'
                                }
                            }
                        },
                        'sass-loader'
                    ]
                }
            ]
        },
        plugins: [
            new webpack.DllReferencePlugin({
                manifest: require(manifest)
            }),
            new CleanWebpackPlugin({verbose: false}),
            new webpack.HashedModuleIdsPlugin({
                hashFunction: 'sha256',
                hashDigest: 'hex',
                hashDigestLength: 20
            }),
            new CopyWebpackPlugin([{from: './package.json', to: ''}]),
            new CaseSensitivePathsPlugin()
        ],
        mode: 'development'
    };

    config.devtool = (argv.mode === 'production') ? 'source-map' : 'eval-source-map';

    if (argv.analyze) {
        config.devtool = 'source-map';
        config.plugins.push(new BundleAnalyzerPlugin());
    }

    return config;
};
