import React from "react";
import { Anchor } from "./Anchor";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Anchor",
  component: Anchor,
};

export const Default = () => <Anchor />;
export const Fill = () => <Anchor fill="blue" />;
export const Size = () => <Anchor height="50" width="50" />;
export const CustomStyle = () => <Anchor style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Anchor className="custom-class" />;
export const Clickable = () => <Anchor onClick={()=>console.log("clicked")} />;

const Template = (args) => <Anchor {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
