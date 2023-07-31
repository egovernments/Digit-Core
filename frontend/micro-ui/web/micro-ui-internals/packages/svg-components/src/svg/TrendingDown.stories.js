import React from "react";
import { TrendingDown } from "./TrendingDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TrendingDown",
  component: TrendingDown,
};

export const Default = () => <TrendingDown />;
export const Fill = () => <TrendingDown fill="blue" />;
export const Size = () => <TrendingDown height="50" width="50" />;
export const CustomStyle = () => <TrendingDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingDown className="custom-class" />;
export const Clickable = () => <TrendingDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <TrendingDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
