#!/usr/bin/env bash

msg() {
    echo -E "/* $1 */"
} 

DOCU_PATH="/docusaurus"

echo -e "Variables:
\\t- RUN_MODE=${RUN_MODE}"


cd "$DOCU_PATH"

if [[ ! -d "$DOCU_PATH"/node_modules ]]; then
    msg "Installing node modules..."
    yarn install &
    [[ "$!" -gt 0 ]] && wait $!
else
    msg "Node modules already exist in $DOCU_PATH/node_modules, continuing..."
fi

cd "$DOCU_PATH"

msg "Will run this Node service as $RUN_MODE mode..."

if [[ "$RUN_MODE" == "build" ]]; then
    msg "Build current sources..."
    yarn run build
elif [[ "$RUN_MODE" == "dev" ]]; then
    msg "Start supervisord to start Docusaurus..."
    supervisord -c /etc/supervisor/conf.d/supervisord.conf
elif [[ "$RUN_MODE" == "upgrade" ]]; then
    msg "Upgrading Docusaurus..."
    yarn upgrade @docusaurus/core@latest @docusaurus/preset-classic@latest
else
    msg "$RUN_MODE unknown!"
fi
