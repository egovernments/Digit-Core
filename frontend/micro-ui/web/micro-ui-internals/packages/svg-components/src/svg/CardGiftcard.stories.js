import React from "react";
import { CardGiftcard } from "./CardGiftcard";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CardGiftcard",
  component: CardGiftcard,
};

export const Default = () => <CardGiftcard />;
export const Fill = () => <CardGiftcard fill="blue" />;
export const Size = () => <CardGiftcard height="50" width="50" />;
export const CustomStyle = () => <CardGiftcard style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CardGiftcard className="custom-class" />;

export const Clickable = () => <CardGiftcard onClick={()=>console.log("clicked")} />;

const Template = (args) => <CardGiftcard {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
