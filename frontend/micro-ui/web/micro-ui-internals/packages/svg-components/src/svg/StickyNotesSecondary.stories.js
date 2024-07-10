import React from "react";
import { StickyNotesSecondary } from "./StickyNotesSecondary";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StickyNotesSecondary",
  component: StickyNotesSecondary,
};

export const Default = () => <StickyNotesSecondary />;
export const Fill = () => <StickyNotesSecondary fill="blue" />;
export const Size = () => <StickyNotesSecondary height="50" width="50" />;
export const CustomStyle = () => <StickyNotesSecondary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StickyNotesSecondary className="custom-class" />;

export const Clickable = () => <StickyNotesSecondary onClick={()=>console.log("clicked")} />;

const Template = (args) => <StickyNotesSecondary {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
