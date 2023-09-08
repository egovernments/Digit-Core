#!/bin/sh

BRANCH="$(git branch --show-current)"

echo "Main Branch: $BRANCH"

INTERNALS="micro-ui-internals"
cd ..

cp workbench/App.js src
cp workbench/package.json package.json 
cp workbench/webpack.config.js webpack.config.js 
cp workbench/inter-package.json $INTERNALS/package.json

cp $INTERNALS/example/src/UICustomizations.js src/Customisations

cd $INTERNALS && echo "UI :: Workbench " && echo "Branch: $(git branch --show-current)" && echo "$(git log -1 --pretty=%B)" && echo "installing packages" && yarn install && echo "starting build" && yarn build && echo "building finished"  && find . -name "node_modules" -type d -prune -print -exec rm -rf '{}' \; 

cd ..

rm -rf node_modules
rm -f yarn.lock

# yarn install
