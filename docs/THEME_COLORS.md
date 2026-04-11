# Theme Color Palette 🎨

## Color Scheme: Grey, Orange, Black/White, and Green

### Primary Colors

#### Orange (Primary Actions)
- **Main Orange**: `#FF6B35`
- **Light Orange**: `#F7931E`
- **Dark Orange**: `#E55A2B`
- **Usage**: Buttons, CTAs, highlights, primary actions

#### Green (Success/Positive)
- **Main Green**: `#48bb78`
- **Light Green**: `#68d391`
- **Dark Green**: `#38a169`
- **Usage**: Success messages, valid inputs, positive indicators

### Neutral Colors

#### Grey Scale
- **Light Grey**: `#f7fafc` - Backgrounds
- **Grey**: `#718096` - Secondary text
- **Dark Grey**: `#4a5568` - Borders, dividers
- **Darker Grey**: `#2d3748` - Headers, dark backgrounds
- **Usage**: Text, backgrounds, borders, cards

#### Black & White
- **Black**: `#1a202c` - Dark mode backgrounds, text
- **White**: `#ffffff` - Light backgrounds, text on dark

### Color Usage Guidelines

#### Buttons
- **Primary**: Orange gradient (`#FF6B35` → `#F7931E`)
- **Secondary**: White with orange border
- **Success**: Green (`#48bb78`)
- **Danger**: Red (keep existing)

#### Backgrounds
- **Light Mode**: White (`#ffffff`) with light grey (`#f7fafc`)
- **Dark Mode**: Black (`#1a202c`) with dark grey (`#2d3748`)
- **Hero Sections**: Grey to orange gradient

#### Text
- **Primary**: Dark grey (`#2d3748`) on light, white on dark
- **Secondary**: Grey (`#718096`)
- **Accent**: Orange for highlights

#### Status Indicators
- **Success**: Green (`#48bb78`)
- **Error**: Red (existing)
- **Warning**: Orange (`#FF6B35`)
- **Info**: Grey (`#718096`)

### CSS Variables

All colors are defined in `styles.css` as CSS variables:

```css
:root {
  --color-orange: #FF6B35;
  --color-orange-light: #F7931E;
  --color-orange-dark: #E55A2B;
  
  --color-green: #48bb78;
  --color-green-light: #68d391;
  --color-green-dark: #38a169;
  
  --color-grey-light: #f7fafc;
  --color-grey: #718096;
  --color-grey-dark: #4a5568;
  --color-grey-darker: #2d3748;
  
  --color-black: #1a202c;
  --color-white: #ffffff;
}
```

### Design Principles

1. **Orange** = Energy, action, primary interactions
2. **Green** = Success, validation, positive feedback
3. **Grey** = Neutrality, professionalism, readability
4. **Black/White** = Contrast, clarity, modern aesthetic
