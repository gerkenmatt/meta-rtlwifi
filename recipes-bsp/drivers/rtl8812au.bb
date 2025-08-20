inherit module
SUMMARY = "Realtek 8812au driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b1918d7d89f091725a3188ff95f7c72b"
SRC_URI = "gitsm://github.com/morrownr/8821au-20210708.git;branch=main;protocol=https"
SRCREV  = "${AUTOREV}" 
S = "${WORKDIR}/git"
DEPENDS = "virtual/kernel"

# Force module target even if Makefile expects a Kconfig symbol
do_configure:append() {
    sed -i -r 's/^obj-\$\(CONFIG_[A-Za-z0-9_]+\)\s*\+=\s*([0-9A-Za-z_]+)\.o/obj-m += \1.o/' ${S}/Makefile || true
    # (harmless if already obj-m)
}

do_compile() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    oe_runmake -C ${S} KSRC=${STAGING_KERNEL_DIR} KDIR=${STAGING_KERNEL_DIR} KERNEL_SRC=${STAGING_KERNEL_DIR} \
               ARCH=arm64 CROSS_COMPILE=${TARGET_PREFIX} V=1
    test -n "$(find ${S} -maxdepth 2 -name '*.ko')" || { echo 'No .ko built'; exit 1; }
}

do_install() {
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
    find ${S} -maxdepth 2 -name '*.ko' -exec install -m0644 {} ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/ \;
    find ${D} -name '*.ko' -exec ${STRIP} --strip-debug {} \;
}

FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*"
RPROVIDES:${PN} += "kernel-module-8821au kernel-module-8811au kernel-module-rtl8811au kernel-module-8821au-${KERNEL_VERSION}"
# Optional autoload (adjust to the actual .ko name after build):
KERNEL_MODULE_AUTOLOAD += "8821au"

