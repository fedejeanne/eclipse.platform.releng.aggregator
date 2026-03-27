#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# verify.sh – Verify that JUNIT.XSL preserves whitespace in failure messages
#
# Works on Linux, macOS, and Windows Subsystem for Linux (WSL).
#
# Usage:
#   cd products/eclipse-junit-tests/src/main/scripts/verify
#   ./verify.sh
#
# The script transforms sample-results.xml with the parent JUNIT.XSL stylesheet
# and opens the resulting HTML file in a browser.
#
# What to look for in the output:
#   In the "Type" column of the testFormatAll01 row you should see:
#
#     contents was not formatted. Actual:
#     package test1;
#     public class E1 {
#         public void foo( Object o ) {
#             String s    = (String)o;   <-- four spaces preserved
#         }
#     }
#
#   WITHOUT the fix, the browser collapses all whitespace sequences to a single
#   space, making "String s    = (String)o;" look like "String s = (String)o;"
#   and the multiline message appears on one line.
#
# Requirements (one of the following):
#   • xsltproc  – sudo apt-get install xsltproc          (Ubuntu/WSL)
#                 brew install libxslt                     (macOS)
#   • python3   – python3 -m pip install lxml
# ---------------------------------------------------------------------------

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
XSL_FILE="$SCRIPT_DIR/../JUNIT.XSL"
XML_FILE="$SCRIPT_DIR/sample-results.xml"
OUT_FILE="$SCRIPT_DIR/report.html"

# Resolve the stylesheet path to an absolute URI (required by xsltproc)
XSL_ABS="$(cd "$(dirname "$XSL_FILE")" && pwd)/$(basename "$XSL_FILE")"

echo "Input XML : $XML_FILE"
echo "Stylesheet: $XSL_ABS"
echo "Output    : $OUT_FILE"
echo ""

# ---------------------------------------------------------------------------
# 1. Transform
# ---------------------------------------------------------------------------
if command -v xsltproc &>/dev/null; then
    echo "Using xsltproc..."
    xsltproc --novalid -o "$OUT_FILE" "$XSL_ABS" "$XML_FILE"

elif python3 -c "from lxml import etree" &>/dev/null 2>&1; then
    echo "Using python3 + lxml..."
    python3 - <<PYEOF
from lxml import etree
xml = etree.parse("$XML_FILE")
xsl = etree.parse("$XSL_ABS")
result = etree.XSLT(xsl)(xml)
with open("$OUT_FILE", "w", encoding="utf-8") as f:
    f.write(str(result))
PYEOF

else
    echo "ERROR: No XSLT processor found." >&2
    echo "" >&2
    echo "Install one of the following and re-run:" >&2
    echo "  Ubuntu/WSL : sudo apt-get install xsltproc" >&2
    echo "  macOS      : brew install libxslt" >&2
    echo "  Python     : python3 -m pip install lxml" >&2
    exit 1
fi

echo "HTML report written to: $OUT_FILE"
echo ""

# ---------------------------------------------------------------------------
# 2. Open in browser
# ---------------------------------------------------------------------------
open_in_browser() {
    local file="$1"

    # WSL: use Windows explorer / default browser via wslview or explorer.exe
    if grep -qiE '(microsoft|wsl)' /proc/version 2>/dev/null; then
        # Convert the Linux path to a Windows path
        local win_path
        win_path="$(wslpath -w "$file" 2>/dev/null || echo "")"
        if [ -n "$win_path" ] && command -v explorer.exe &>/dev/null; then
            echo "Opening in Windows default browser via explorer.exe..."
            explorer.exe "$win_path"
            return
        fi
    fi

    # macOS
    if command -v open &>/dev/null; then
        open "$file"
        return
    fi

    # Linux (X11/Wayland)
    if command -v xdg-open &>/dev/null; then
        xdg-open "$file"
        return
    fi

    echo "Could not open the browser automatically."
    echo "Open the following file manually in your browser:"
    echo "  $file"
}

open_in_browser "$OUT_FILE" || true
