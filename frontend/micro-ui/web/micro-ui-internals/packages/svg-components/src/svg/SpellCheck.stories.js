import React from "react";
import { SpellCheck } from "./SpellCheck";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SpellCheck",
  component: SpellCheck,
};

export const Default = () => <SpellCheck />;
export const Fill = () => <SpellCheck fill="blue" />;
export const Size = () => <SpellCheck height="50" width="50" />;
export const CustomStyle = () => <SpellCheck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SpellCheck className="custom-class" />;

export const Clickable = () => <SpellCheck onClick={()=>console.log("clicked")} />;

const Template = (args) => <SpellCheck {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
