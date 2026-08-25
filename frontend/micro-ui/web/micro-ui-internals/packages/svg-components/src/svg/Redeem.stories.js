import React from "react";
import { Redeem } from "./Redeem";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Redeem",
  component: Redeem,
};

export const Default = () => <Redeem />;
export const Fill = () => <Redeem fill="blue" />;
export const Size = () => <Redeem height="50" width="50" />;
export const CustomStyle = () => <Redeem style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Redeem className="custom-class" />;

export const Clickable = () => <Redeem onClick={()=>console.log("clicked")} />;

const Template = (args) => <Redeem {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
