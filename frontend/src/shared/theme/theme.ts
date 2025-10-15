import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#5B8CFF' },
    secondary: { main: '#9B7BFF' },
    success: { main: '#3CCB7F' },
    warning: { main: '#F6B44D' },
    error: { main: '#FF6B6B' },
    info: { main: '#5BC8FF' },
    background: {
      default: '#0E1116',
      paper: 'rgba(22,25,32,0.7)'
    },
    divider: 'rgba(255,255,255,0.08)',
    action: {
      hover: 'rgba(255,255,255,0.06)',
      selected: 'rgba(255,255,255,0.08)',
      disabled: 'rgba(255,255,255,0.3)',
      disabledBackground: 'rgba(255,255,255,0.06)',
      focus: 'rgba(91,140,255,0.28)',
      active: 'rgba(255,255,255,0.72)'
    }
  },
  typography: {
    fontFamily: `Inter, Manrope, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Apple Color Emoji","Segoe UI Emoji"`,
    h1: { fontWeight: 800, letterSpacing: '-0.02em', fontSize: 'clamp(28px, 3.2vw, 42px)' },
    h2: { fontWeight: 800, letterSpacing: '-0.02em', fontSize: 'clamp(24px, 2.6vw, 36px)' },
    h3: { fontWeight: 700, letterSpacing: '-0.01em', fontSize: 'clamp(20px, 2.2vw, 28px)' },
    h4: { fontWeight: 700, letterSpacing: '-0.01em' },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    subtitle1: { opacity: 0.9 },
    body1: { lineHeight: 1.6 },
    body2: { lineHeight: 1.55, opacity: 0.9 },
    button: { fontWeight: 700, letterSpacing: 0.2 }
  },
  shape: { borderRadius: 16 },
  spacing: 8,
  zIndex: {
    appBar: 1200,
    drawer: 1100,
    modal: 1300,
    tooltip: 1500
  },
  transitions: {
    duration: { shortest: 120, shorter: 160, short: 200, standard: 240, complex: 320, enteringScreen: 220, leavingScreen: 200 },
    easing: {
      easeInOut: 'cubic-bezier(.4,0,.2,1)',
      easeOut: 'cubic-bezier(0,0,.2,1)',
      easeIn: 'cubic-bezier(.4,0,1,1)',
      sharp: 'cubic-bezier(.4,0,.6,1)'
    }
  },
  shadows: [
    'none',
    '0 2px 10px rgba(0,0,0,0.35)',
    '0 6px 18px rgba(0,0,0,0.35)',
    '0 10px 30px rgba(0,0,0,0.4)',
    '0 16px 40px rgba(0,0,0,0.45)',
    '0 22px 60px rgba(0,0,0,0.5)',
    ...Array(19).fill('0 22px 60px rgba(0,0,0,0.5)')
  ] as any,
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        ':root': {
          '--ring': '0 0 0 3px rgba(91,140,255,0.35)',
          '--glass': 'linear-gradient(180deg, rgba(255,255,255,0.04), rgba(255,255,255,0.01))'
        },
        body: {
          background:
            'radial-gradient(1200px 600px at 10% -10%, rgba(91,140,255,0.15), transparent 60%),' +
            'radial-gradient(900px 500px at 90% 10%, rgba(155,123,255,0.12), transparent 60%),' +
            'linear-gradient(180deg, #0E1116 0%, #0E1116 100%)',
          backdropFilter: 'saturate(110%)'
        },
        '*::selection': { background: 'rgba(91,140,255,0.35)' },
        '*::-webkit-scrollbar': { width: 10, height: 10 },
        '*::-webkit-scrollbar-thumb': {
          backgroundColor: 'rgba(255,255,255,0.18)',
          borderRadius: 8,
          border: '2px solid rgba(14,17,22,0.8)'
        },
        '*::-webkit-scrollbar-track': { background: 'transparent' }
      }
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          backgroundImage: 'var(--glass)',
          border: '1px solid rgba(255,255,255,0.06)',
          boxShadow: '0 10px 30px rgba(0,0,0,0.45), inset 0 1px 0 rgba(255,255,255,0.03)',
          backdropFilter: 'blur(8px)'
        }
      }
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          background:
            'linear-gradient(90deg, rgba(91,140,255,0.25) 0%, rgba(155,123,255,0.25) 100%), #121620',
          boxShadow: '0 8px 24px rgba(0,0,0,0.55)',
          borderBottom: '1px solid rgba(255,255,255,0.06)',
          backdropFilter: 'blur(10px)'
        }
      }
    },
    MuiToolbar: { styleOverrides: { root: { minHeight: 64, gap: 8 } } },
    MuiButtonBase: { defaultProps: { disableTouchRipple: true } },
    MuiButton: {
      defaultProps: { size: 'medium' },
      styleOverrides: {
        root: {
          borderRadius: 12,
          textTransform: 'none',
          fontWeight: 700,
          transition: 'transform .14s ease, box-shadow .14s ease, background .14s ease',
          '&:active': { transform: 'translateY(0)' },
          '&:focus-visible': { boxShadow: 'var(--ring)' }
        },
        containedPrimary: {
          background: 'linear-gradient(180deg, #6CA0FF 0%, #5B8CFF 60%, #4779FF 100%)',
          boxShadow: '0 8px 24px rgba(75,126,255,0.35)',
          '&:hover': { transform: 'translateY(-1px)', boxShadow: '0 12px 32px rgba(75,126,255,0.45)' }
        },
        containedSecondary: {
          background: 'linear-gradient(180deg, #B198FF 0%, #9B7BFF 60%, #7F5CFF 100%)',
          boxShadow: '0 8px 24px rgba(155,123,255,0.35)',
          '&:hover': { transform: 'translateY(-1px)', boxShadow: '0 12px 32px rgba(155,123,255,0.45)' }
        },
        outlined: {
          borderColor: 'rgba(255,255,255,0.18)',
          background: 'rgba(255,255,255,0.03)',
          '&:hover': { borderColor: 'rgba(255,255,255,0.28)', background: 'rgba(255,255,255,0.06)' }
        }
      },
      variants: [
        {
          props: { variant: 'soft' as any },
          style: {
            background: 'rgba(91,140,255,0.12)',
            color: '#CFE0FF',
            border: '1px solid rgba(91,140,255,0.25)',
            boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.06)',
            '&:hover': { background: 'rgba(91,140,255,0.18)' }
          }
        },
        {
          props: { variant: 'glass' as any },
          style: {
            backgroundImage: 'var(--glass)',
            border: '1px solid rgba(255,255,255,0.08)',
            '&:hover': { background: 'rgba(255,255,255,0.05)' }
          }
        }
      ]
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          '&:focus-visible': { boxShadow: 'var(--ring)' },
          background: 'rgba(255,255,255,0.02)',
          border: '1px solid rgba(255,255,255,0.06)',
          '&:hover': { background: 'rgba(255,255,255,0.06)' }
        }
      }
    },
    MuiTextField: {
      defaultProps: { size: 'medium', variant: 'outlined' },
      styleOverrides: {
        root: {
          background: 'rgba(255,255,255,0.02)',
          borderRadius: 12,
          transition: 'box-shadow .12s ease, background .12s ease'
        }
      }
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.12)' },
          '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.22)' },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(91,140,255,0.6)' },
          '&.Mui-focused': { boxShadow: 'var(--ring)' }
        },
        input: { paddingTop: 14, paddingBottom: 14 }
      }
    },
    MuiDivider: { styleOverrides: { root: { borderColor: 'rgba(255,255,255,0.08)' } } },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          borderRadius: 16,
          backgroundImage: 'var(--glass)',
          border: '1px solid rgba(255,255,255,0.06)',
          boxShadow: '0 10px 30px rgba(0,0,0,0.45), inset 0 1px 0 rgba(255,255,255,0.03)'
        }
      },
      variants: [
        { props: { variant: 'outlined' }, style: { background: 'rgba(255,255,255,0.02)' } },
        {
          props: { variant: 'gradient' as any },
          style: {
            background: 'linear-gradient(180deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0.02) 100%)',
            border: '1px solid rgba(255,255,255,0.08)'
          }
        }
      ]
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          borderRadius: 10,
          background: 'rgba(30,34,44,0.9)',
          border: '1px solid rgba(255,255,255,0.08)',
          backdropFilter: 'blur(6px)'
        }
      }
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundImage: 'var(--glass)',
          border: '1px solid rgba(255,255,255,0.08)',
          boxShadow: '0 22px 60px rgba(0,0,0,0.56)',
          backdropFilter: 'blur(10px)'
        }
      }
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          '&.Mui-selected': {
            background: 'rgba(91,140,255,0.14)',
            '&:hover': { background: 'rgba(91,140,255,0.2)' }
          }
        }
      }
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          background: 'rgba(255,255,255,0.06)',
          border: '1px solid rgba(255,255,255,0.08)'
        }
      }
    },
    MuiLinearProgress: { styleOverrides: { root: { height: 6, borderRadius: 999 }, bar: { borderRadius: 999 } } },
    MuiSwitch: {
      styleOverrides: {
        switchBase: { '&.Mui-checked + .MuiSwitch-track': { opacity: 0.5 } },
        track: { background: 'rgba(255,255,255,0.12)' }
      }
    }
  }
})
