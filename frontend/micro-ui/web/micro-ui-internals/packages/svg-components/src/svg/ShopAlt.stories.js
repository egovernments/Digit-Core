import React from "react";
import { ShopAlt } from "./ShopAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ShopAlt",
  component: ShopAlt,
};

export const Default = () => <ShopAlt />;
export const Fill = () => <ShopAlt fill="blue" />;
export const Size = () => <ShopAlt height="50" width="50" />;
export const CustomStyle = () => <ShopAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ShopAlt className="custom-class" />;

export const Clickable = () => <ShopAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ShopAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
