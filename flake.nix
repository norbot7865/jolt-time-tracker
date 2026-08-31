{
  description = "Reproducible Jolt Time Tracker development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    # This is the full revision recorded in deps.edn and DEVELOPMENT.md.
    jolt = {
      url = "git+https://github.com/jasalt/jolt.git?rev=447b874d06066d15fee187200fabaf410f4ff5b6&submodules=1";
      flake = false;
    };
  };

  outputs = { self, nixpkgs, jolt }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" ];
      forAllSystems = nixpkgs.lib.genAttrs systems;
    in {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs { inherit system; };
        in {
          default = pkgs.mkShell {
            packages = [
              pkgs.chez
              pkgs.clj-kondo
              pkgs.gcc
              pkgs.gnumake
              pkgs.git
              pkgs.ncurses
              pkgs.pkg-config
              pkgs.unzip
              pkgs.xxd
              pkgs.zlib
              pkgs.lz4
            ] ++ pkgs.lib.optionals pkgs.stdenv.hostPlatform.isLinux [
              pkgs.libuuid
            ] ++ pkgs.lib.optionals pkgs.stdenv.hostPlatform.isDarwin [
              pkgs.libiconv
            ];

            shellHook = ''
              export JOLT_BIN=${jolt}/bin/jolt
              # Jolt's source launcher expects a `chez` command; Nix names it `scheme`.
              export JOLT_CHEZ=${pkgs.chez}/bin/scheme
              export JOLT_VERSION=v0.7.28-45-g447b874d
              export JOLT_NO_USER_DEPS=1
              export JOLT_OPENSSL_LIBDIR=${pkgs.lib.makeLibraryPath [ pkgs.openssl ]}
              export SSL_CERT_FILE=${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt
              export GIT_SSL_CAINFO="$SSL_CERT_FILE"
            '';
          };
        });
    };
}
