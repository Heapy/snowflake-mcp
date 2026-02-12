#!/bin/sh
set -e

INSTALL_DIR="$HOME/.snowflake-mcp"
DIST_DIR="$INSTALL_DIR/dist"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Building distribution..."
"$SCRIPT_DIR/gradlew" -p "$SCRIPT_DIR" installDist

echo "Installing to $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR/bin"

# Only delete the distribution folder — user files (db, pre-start.sh) stay untouched
rm -rf "$DIST_DIR"
cp -r "$SCRIPT_DIR/app/build/install/snowflake-mcp" "$DIST_DIR"

# Add to PATH via shell profile if not already there
PATH_LINE='export PATH="$HOME/.snowflake-mcp/bin:$PATH"'
SHELL_PROFILE="$HOME/.zshrc"

if ! grep -qF '.snowflake-mcp/bin' "$SHELL_PROFILE" 2>/dev/null; then
    echo "" >> "$SHELL_PROFILE"
    echo "$PATH_LINE" >> "$SHELL_PROFILE"
    echo "Added to PATH in $SHELL_PROFILE — restart your shell or run:"
    echo "  source $SHELL_PROFILE"
else
    echo "PATH already configured in $SHELL_PROFILE"
fi

# Create wrapper script that sources pre-start and runs app
cat > "$INSTALL_DIR/bin/sfmcp" << 'EOF'
#!/bin/sh
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PRE_START="$BASE_DIR/pre-start.sh"
if [ -f "$PRE_START" ]; then
    . "$PRE_START"
fi
export SNOWFLAKE_MCP_DB_PATH="$BASE_DIR/snowflake-mcp.db"
exec "$BASE_DIR/dist/bin/snowflake-mcp"
EOF
chmod +x "$INSTALL_DIR/bin/sfmcp"

echo "Done. Run 'sfmcp' to start."
