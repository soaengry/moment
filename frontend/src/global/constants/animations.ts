/**
 * Framer Motion Animation Variants
 *
 * 웨딩 플랫폼 특성에 맞는 부드럽고 우아한 애니메이션 정의
 */

import type { Variants, Transition } from 'framer-motion';

// ============================================
// Transition Presets
// ============================================

export const springTransition: Transition = {
  type: 'spring',
  stiffness: 260,
  damping: 20,
};

export const smoothTransition: Transition = {
  duration: 0.4,
  ease: [0.4, 0, 0.2, 1], // cubic-bezier easing
};

export const slowTransition: Transition = {
  duration: 0.6,
  ease: [0.4, 0, 0.2, 1],
};

// ============================================
// Basic Animations
// ============================================

export const fadeIn: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: smoothTransition,
  },
};

export const fadeInSlow: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: slowTransition,
  },
};

export const slideUp: Variants = {
  hidden: { opacity: 0, y: 30 },
  visible: {
    opacity: 1,
    y: 0,
    transition: smoothTransition,
  },
};

export const slideDown: Variants = {
  hidden: { opacity: 0, y: -30 },
  visible: {
    opacity: 1,
    y: 0,
    transition: smoothTransition,
  },
};

export const slideInRight: Variants = {
  hidden: { opacity: 0, x: 30 },
  visible: {
    opacity: 1,
    x: 0,
    transition: smoothTransition,
  },
};

export const slideInLeft: Variants = {
  hidden: { opacity: 0, x: -30 },
  visible: {
    opacity: 1,
    x: 0,
    transition: smoothTransition,
  },
};

export const scaleIn: Variants = {
  hidden: { opacity: 0, scale: 0.95 },
  visible: {
    opacity: 1,
    scale: 1,
    transition: springTransition,
  },
};

// ============================================
// Stagger Container
// ============================================

export const staggerContainer: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.05,
    },
  },
};

export const staggerFastContainer: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0,
    },
  },
};

export const staggerSlowContainer: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.15,
      delayChildren: 0.1,
    },
  },
};

// Stagger child item
export const staggerItem: Variants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: smoothTransition,
  },
};

// ============================================
// Modal & Bottom Sheet
// ============================================

export const modalBackdrop: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { duration: 0.2 },
  },
  exit: {
    opacity: 0,
    transition: { duration: 0.2, delay: 0.1 },
  },
};

export const modalContent: Variants = {
  hidden: { opacity: 0, scale: 0.95, y: 20 },
  visible: {
    opacity: 1,
    scale: 1,
    y: 0,
    transition: springTransition,
  },
  exit: {
    opacity: 0,
    scale: 0.95,
    y: 20,
    transition: { duration: 0.2 },
  },
};

export const bottomSheet: Variants = {
  hidden: { y: '100%' },
  visible: {
    y: 0,
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 30,
    },
  },
  exit: {
    y: '100%',
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 30,
    },
  },
};

// ============================================
// Interactive Elements
// ============================================

export const buttonHover = {
  scale: 1.02,
  transition: { duration: 0.2 },
};

export const buttonTap = {
  scale: 0.98,
};

export const buttonHoverLift = {
  scale: 1.05,
  y: -2,
  transition: { duration: 0.2 },
};

export const iconScale = {
  scale: [1, 1.3, 1],
  transition: { duration: 0.4 },
};

// ============================================
// Special Effects
// ============================================

// Ken Burns effect for images
export const kenBurns: Variants = {
  hidden: { scale: 1, opacity: 0 },
  visible: {
    scale: 1.1,
    opacity: 1,
    transition: {
      scale: { duration: 10, ease: 'linear' },
      opacity: { duration: 1 },
    },
  },
  exit: {
    opacity: 0,
    transition: { duration: 1 },
  },
};

// Shake effect for errors
export const shake = {
  x: [-5, 5, -5, 5, 0],
  transition: { duration: 0.4 },
};

// Pulse effect
export const pulse: Variants = {
  initial: { scale: 1 },
  animate: {
    scale: [1, 1.05, 1],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

// ============================================
// Page Transitions
// ============================================

export const pageTransition: Variants = {
  initial: { opacity: 0, x: -20 },
  animate: {
    opacity: 1,
    x: 0,
    transition: smoothTransition,
  },
  exit: {
    opacity: 0,
    x: 20,
    transition: smoothTransition,
  },
};

// ============================================
// Expand/Collapse
// ============================================

export const expandCollapse: Variants = {
  collapsed: {
    height: 0,
    opacity: 0,
    transition: smoothTransition,
  },
  expanded: {
    height: 'auto',
    opacity: 1,
    transition: smoothTransition,
  },
};

// ============================================
// Loading Spinner
// ============================================

export const spinnerRotate = {
  rotate: 360,
  transition: {
    duration: 1,
    repeat: Infinity,
    ease: "linear" as const,
  },
} as const;
