using import C.stdlib print String
using import radl.strfmt

obj-dir := f"${module-dir}/dist/obj"
bin-dir := f"${module-dir}/dist/bin"

module := require-from module-dir ".src.main" __env
main := (typify (module as Closure) i32 (@ rawstring))

inline execute (cmd)
    print2 "+" cmd
    system f"bash -c \"${cmd}\""

compile-object
    default-target-triple
    compiler-file-kind-object
    f"${obj-dir}/game.o" as string
    do
        let main
        local-scope;

libs... :=
    va-map
        inline (libname)
            let filename =
                static-match operating-system
                case 'windows
                    f"${libname}.dll"
                case 'linux
                    f"lib${libname}.so"
                default
                    error "unsupported OS"
            filename as string
        _
            "physfs"
            "fontdue_native"
            "miniaudio"
            "SDL2"
            "stb"
            "wgpu_native"
            "cimgui"

libpaths... :=
    va-map
        inline get-libpath (libname)
            find-library libname __env.library-search-path
        libs...

va-map
    inline copy-lib (path)
        execute f"cp \"${path}\" ./dist/bin"
    libpaths...

LDFLAGS :=
    static-fold (libs = S"") for lib in (va-each libs...)
        f"${libs} -l:${lib} "

let exe-name =
    static-match operating-system
    case 'windows
        "game.exe"
    default
        "game"

execute f"gcc -o ${bin-dir}/${exe-name} ${obj-dir}/game.o -I${module-dir}/include -lm -L${bin-dir} ${LDFLAGS} -Wl,-rpath -Wl,'\$ORIGIN'"
