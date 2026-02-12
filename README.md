# snowflake-mcp

MCP server that gives Claude (and other MCP clients) access to your Snowflake databases.

## Features

- **4 MCP tools** — `execute_sql`, `list_connections`, `get_active_connection`, `set_read_only`
- **Read-only mode** — enabled by default, blocks mutation queries (INSERT, UPDATE, DROP, etc.)
- **Multiple connections** — configure several Snowflake connections, switch between them
- **Web UI** — manage connections, view query history, toggle settings at `localhost:33300`
- **Query history** — every query executed through MCP is recorded with timing and results

## Setup

Requires Java 25+.

```bash
./install.sh
```

This builds the project, installs to `~/.snowflake-mcp/`, and creates the `sfmcp` command.

## Usage

Start the server:

```bash
sfmcp
```

Add to Claude Code:

```bash
claude mcp add --transport sse snowflake-mcp http://localhost:33300
```

Then open `http://127.0.0.1:33300/connections` to add your Snowflake connection.

## Development

```bash
./gradlew run          # run locally
./gradlew test         # run tests
./gradlew installDist  # build distribution
```

## Tech Stack

Kotlin, Ktor, Exposed (SQLite), Snowflake JDBC, MCP SDK, HTMX
