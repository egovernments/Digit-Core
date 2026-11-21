import React from "react";
import { SportsVolleyball } from "./SportsVolleyball";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsVolleyball",
  component: SportsVolleyball,
};

export const Default = () => <SportsVolleyball />;
export const Fill = () => <SportsVolleyball fill="blue" />;
export const Size = () => <SportsVolleyball height="50" width="50" />;
export const CustomStyle = () => <SportsVolleyball style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsVolleyball className="custom-class" />;

export const Clickable = () => <SportsVolleyball onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsVolleyball {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
