---
name: "Gestión de pólizas"
description: "Herramienta contable sobria basada en líneas, jerarquía y datos alineados."
colors:
  ink: "#1C2A30"
  primary: "#245D63"
  background: "#F6F8F7"
  border: "#CDD8D8"
  success: "#2F6B4F"
  danger: "#963B3B"
typography:
  sans:
    fontFamily: '"Segoe UI", system-ui, sans-serif'
  data:
    fontFamily: 'ui-monospace, "Cascadia Mono", Consolas, monospace'
rounded:
  DEFAULT: "0.35rem"
omitted:
  - section: spacing
    reason: "Bootstrap conserva la escala de espaciado del proyecto."
components:
  upload:
    backgroundColor: "$colors.background"
    textColor: "$colors.ink"
  primary-action:
    backgroundColor: "$colors.primary"
  status-success:
    textColor: "$colors.success"
  alert-error:
    textColor: "$colors.danger"
---

# Gestión de pólizas

## Overview

Producto administrativo en español para importar operaciones y consultar pólizas. Su referencia es un libro mayor contemporáneo: líneas claras, superficies planas y cifras precisas. La firma visual es la banda Debe/Haber con cierre de doble línea. Evitar dashboards SaaS, gráficas y acumulación de tarjetas. Los tokens de este archivo reflejan `src/assets/main.css`, que es la fuente de ejecución.

## Components

- Navegación horizontal, sin sidebar.
- Tablas HTML semánticas con desplazamiento horizontal en pantallas estrechas.
- Importes alineados a la derecha, con dos decimales y números tabulares.
- Jerarquía mediante bordes y divisores antes que sombras.
- Movimiento mínimo y compatible con `prefers-reduced-motion`.
