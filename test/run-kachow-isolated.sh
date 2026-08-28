#!/bin/sh

# Run Kachow outside the repository so its persistent data cannot leak between UI cases.
classes="$PWD/_temp/ui-test-console-classes"

if [ "$#" -eq 1 ]; then
    test_directory="$PWD/$1"
else
    test_directory=$(mktemp -d "${TMPDIR:-/tmp}/kachow-ui-test.XXXXXX") || exit 1
fi

cd "$test_directory" || exit 1
exec java -cp "$classes" com.benthecat.kachow.Kachow
