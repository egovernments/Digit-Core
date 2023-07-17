import React from "react";
import { RestaurantMenu } from "./RestaurantMenu";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RestaurantMenu",
  component: RestaurantMenu,
};

export const Default = () => <RestaurantMenu />;
export const Fill = () => <RestaurantMenu fill="blue" />;
export const Size = () => <RestaurantMenu height="50" width="50" />;
export const CustomStyle = () => <RestaurantMenu style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RestaurantMenu className="custom-class" />;

export const Clickable = () => <RestaurantMenu onClick={()=>console.log("clicked")} />;

const Template = (args) => <RestaurantMenu {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
