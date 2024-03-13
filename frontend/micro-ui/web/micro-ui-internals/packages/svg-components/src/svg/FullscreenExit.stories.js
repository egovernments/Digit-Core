import React from "react";
import { FullscreenExit } from "./FullscreenExit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FullscreenExit",
  component: FullscreenExit,
};

export const Default = () => <FullscreenExit />;
export const Fill = () => <FullscreenExit fill="blue" />;
export const Size = () => <FullscreenExit height="50" width="50" />;
export const CustomStyle = () => <FullscreenExit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FullscreenExit className="custom-class" />;

export const Clickable = () => <FullscreenExit onClick={()=>console.log("clicked")} />;

const Template = (args) => <FullscreenExit {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
