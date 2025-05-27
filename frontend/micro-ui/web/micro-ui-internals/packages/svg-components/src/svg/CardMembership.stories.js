import React from "react";
import { CardMembership } from "./CardMembership";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CardMembership",
  component: CardMembership,
};

export const Default = () => <CardMembership />;
export const Fill = () => <CardMembership fill="blue" />;
export const Size = () => <CardMembership height="50" width="50" />;
export const CustomStyle = () => <CardMembership style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CardMembership className="custom-class" />;

export const Clickable = () => <CardMembership onClick={()=>console.log("clicked")} />;

const Template = (args) => <CardMembership {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
